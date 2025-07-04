package com.nayonikaeyecare.api.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.nayonikaeyecare.api.dto.user.AuthenticationRequest;
import com.nayonikaeyecare.api.dto.user.AuthenticationResponse;
import com.nayonikaeyecare.api.dto.user.OTPResendRequest;
import com.nayonikaeyecare.api.dto.user.UserRequest;
import com.nayonikaeyecare.api.dto.user.OTPVerificationRequest;
import com.nayonikaeyecare.api.entities.application.Application;
import com.nayonikaeyecare.api.entities.user.User;
import com.nayonikaeyecare.api.entities.user.UserCredential;
import com.nayonikaeyecare.api.entities.user.UserSession;
import com.nayonikaeyecare.api.entities.user.UserSessionStatus;
import com.nayonikaeyecare.api.entities.user.UserStatus;
import com.nayonikaeyecare.api.repositories.application.ApplicationRepository;
import com.nayonikaeyecare.api.repositories.user.UserCredentialRepository;
import com.nayonikaeyecare.api.exceptions.InvalidApplicationCodeException; // Added
import com.nayonikaeyecare.api.repositories.user.UserNotFoundException;
import com.nayonikaeyecare.api.repositories.user.UserRepository;
import com.nayonikaeyecare.api.repositories.user.UserSessionRepository;
import com.nayonikaeyecare.api.security.JWTTokenProvider;
import java.util.Date;
// import com.nayonikaeyecare.api.entities.User; // Already imported via com.nayonikaeyecare.api.entities.user.User

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

@Service

@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final ApplicationRepository applicationRepository;
    private final UserSessionRepository userSessionRepository;
    private final JWTTokenProvider jwtTokenProvider;
    private final SmsService smsService;

    /**
     * creates a new user in the system
     * 
     * @param userRequest
     * @throws IllegalArgumentException if userRequest is null
     * @throws UserNotFoundException    if no user is found with the given
     */

    public void createUser(UserRequest userRequest) {

        User newUser = User.builder()
                .firstName(userRequest.firstName())
                .lastName(userRequest.lastName())
                .phoneNumber(userRequest.phoneNumber())
                .createdAt(new java.util.Date())
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(newUser);
    }

    /**
     * Updates an existing user's information
     * 
     * @param userId      the ID of the user to update
     * @param userRequest the updated user information
     * @return the updated User object
     * @throws UserNotFoundException if user with given ID is not found
     */
    public User updateUser(String userId, UserRequest userRequest) {
        User existingUser = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Update the user fields if they are provided in the request
        if (userRequest.firstName() != null) {
            existingUser.setFirstName(userRequest.firstName());
        }
        if (userRequest.lastName() != null) {
            existingUser.setLastName(userRequest.lastName());
        }
        if (userRequest.phoneNumber() != null) {
            existingUser.setPhoneNumber(userRequest.phoneNumber());
        }
        if (userRequest.email() != null) {
            existingUser.setEmail(userRequest.email());
        }
        if (userRequest.city() != null) {
            existingUser.setCity(userRequest.city());
        }
        if (userRequest.state() != null) {
            existingUser.setState(userRequest.state());
        }

        return userRepository.save(existingUser);
    }

    /**
     * authenticates the user and generatinos a usersession if the credentials are
     * valid
     * 
     * @param authenticationRequest
     * @return
     */
    @Nonnull
    public AuthenticationResponse authenticateUser(@Nonnull AuthenticationRequest authenticationRequest) {
        String applicationCode = authenticationRequest.applicationCode();
        String credential = authenticationRequest.credential(); // This is the mobile number

        if ("WEB_PORTAL".equals(applicationCode)) {
            userRepository.findByPhoneNumber(credential)
                    .orElseThrow(() -> new UserNotFoundException(
                            "User not found with the provided mobile number. Please contact the application administrator."));
            // If user exists, proceed with normal OTP logic
        } else if (!"MOBILE_APP".equals(applicationCode)) {
            // Neither WEB_PORTAL nor MOBILE_APP
            throw new InvalidApplicationCodeException("Invalid applicationCode: " + applicationCode);
        }
        // For MOBILE_APP, or if WEB_PORTAL user check passed, continue with existing logic

        UserCredential userCredential = userCredentialRepository.findByCredential(credential);
        boolean userCreated = false;
        if (userCredential == null) {
            // check if the user can be created for the application because of
            // autoregistration
            // Note: checkAndCreateUserForApplication itself throws exceptions if app code is invalid or auto-reg is off
            userCreated = checkAndCreateUserForApplication(authenticationRequest, userCredential);
            userCredential = userCredentialRepository.findByCredential(credential);
        } else {
            // user credential already exists hence no need to create a new user
            // Check if phone number is null for existing user linked to this credential
            User existingUser = userRepository.findByUserCredentialId(userCredential.getId());
            if (existingUser != null) {
                userCreated = existingUser.getPhoneNumber() == null;
            } else {
                // This case should ideally not happen if data integrity is maintained
                // (UserCredential exists but no corresponding User).
                // If it does, treat as if user needs to be fully "created" or associated.
                userCreated = true; 
            }
        }

        // check if the user credential is not null (either found or created)
        if (userCredential != null) {
            String otp = generateOtp();
            User user = userRepository.findByUserCredentialId(userCredential.getId());
            
            // Ensure user is found, especially after potential creation.
            if (user == null) {
                // This would indicate an issue with user creation logic or data consistency.
                throw new UserNotFoundException("User could not be determined for the credential.");
            }

            UserSession newUserSession = UserSession.builder()
                    .userId(user.getId())
                    .applicationCode(applicationCode)
                    .otp(otp)
                    .status(UserSessionStatus.INITIATIED)
                    .createdAt(new java.util.Date())
                    .build();
            UserSession savedUserSession = userSessionRepository.save(newUserSession);

            // Retrieve the full User object to get the phone number for sending OTP
            // User object obtained above might be sufficient if it's consistently populated
            User fullUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new UserNotFoundException(
                            "User not found after session creation with id: " + user.getId()));

            if (fullUser.getPhoneNumber() != null && !fullUser.getPhoneNumber().isEmpty()) {
                smsService.sendOtp(fullUser.getPhoneNumber(), otp);
            }

            return new AuthenticationResponse(savedUserSession.getId().toString(),
                    userCreated, user.getId().toString());
        } else {
            // This block should ideally not be reached if checkAndCreateUserForApplication
            // throws for invalid app codes or if WEB_PORTAL check already failed.
            // It might be reached if MOBILE_APP tries to register and auto-registration is off for MOBILE_APP.
            Application application = applicationRepository.findByCode(applicationCode);
            if (application == null || !application.isAllowAutoRegistration()) {
                 throw new UserNotFoundException("User not found and auto-registration is not permitted for this application.");
            }
            // Fallback, though logic above should handle specific cases.
            throw new UserNotFoundException("Invalid credentials or user creation failed.");
        }
    }

    /**
     * resendOTP helps to resend the OTP to the user
     * 
     * @param otpResendRequest
     * @return
     */
    public UserSession resendOTP(@Nonnull OTPResendRequest otpResendRequest) {

        UserSession userSession = userSessionRepository
                .findById(new ObjectId(otpResendRequest.sessionId()))
                .orElseThrow(() -> new UserNotFoundException("User session not found"));

        if (userSession.getStatus() == UserSessionStatus.OTP_EXPIRED ||
                userSession.getStatus() == UserSessionStatus.OTP_EXPIRED_FOR_RESEND ||
                userSession.getStatus() == UserSessionStatus.OTP_VERIFIED) {
            // OTP is expired or regenerated, throw an error
            throw new IllegalArgumentException("Invalid Session");
        }

        userSession.setStatus(UserSessionStatus.OTP_EXPIRED_FOR_RESEND);
        userSession.setUpdatedAt(new java.util.Date());
        userSessionRepository.save(userSession);

        // create a new session for the user
        String otp = generateOtp();
        UserSession newUserSession = UserSession.builder().applicationCode(userSession.getApplicationCode())
                .userId(userSession.getUserId())
                .otp(otp)
                .status(UserSessionStatus.OTP_REGENERATED)
                .linkedSessionId(userSession.getId())
                .build();
        userSessionRepository.save(newUserSession);

        // Retrieve the full User object to get the phone number
        User fullUser = userRepository.findById(newUserSession.getUserId())
            .orElseThrow(() -> new UserNotFoundException("User not found for OTP resend with id: " + newUserSession.getUserId()));
        
        if (fullUser.getPhoneNumber() != null && !fullUser.getPhoneNumber().isEmpty()) {
            smsService.sendOtp(fullUser.getPhoneNumber(), otp);
        }

        return newUserSession;
    }

    /**
     * generate OTP helps to generate a random number of length 4
     * 
     * @param authenticationRequest
     * @return
     */
    private String generateOtp() {
        String otp = generateNonZeroRandomNumber(4);
        return otp;

    }

    /**
     * checkAndCreateUserForApplication checks if the user can be created for the
     * 
     * @param authenticationRequest
     * @param userCredential
     * @return
     */

    private boolean checkAndCreateUserForApplication(AuthenticationRequest authenticationRequest,
            UserCredential userCredentialInput) { // Renamed parameter to avoid confusion with outer scope
        Application application = applicationRepository
                .findByCode(authenticationRequest.applicationCode());
        boolean userJustCreated = false; // More descriptive variable name
        if (application != null) {
            if (application.isAllowAutoRegistration()) {
                // User does not exist, but auto-registration is allowed for this application.
                // Create a new user credential if it wasn't found before.
                UserCredential newUserCredential = UserCredential.builder()
                        .credential(authenticationRequest.credential())
                        .build();
                userCredentialRepository.save(newUserCredential);
                // Create a new user with the provided details
                User user = User.builder()
                        .userCredentialId(newUserCredential.getId())
                        .phoneNumber(newUserCredential.getCredential()) // Set phone number from credential
                        .createdAt(new java.util.Date())
                        .status(UserStatus.ACTIVE) // Default status
                        .build();
                userRepository.save(user);
                userJustCreated = true;
                return userJustCreated;

            } else {
                // application does not allow for automatic creation of user credentials,
                // and the user was not found by credential earlier.
                throw new UserNotFoundException(
                        "User not found and auto-registration is not allowed for this application.");
            }
        } else {
            // application code itself was invalid (no application found for this code)
            // This case is now handled by the initial check in authenticateUser for non WEB_PORTAL/MOBILE_APP codes.
            // However, keeping a safeguard here if checkAndCreateUserForApplication is called directly elsewhere
            // or if MOBILE_APP has an invalid application code in its configuration (which shouldn't happen).
            throw new InvalidApplicationCodeException(
                    "Application code " + authenticationRequest.applicationCode() + " is invalid.");
        }
    }

    private String generateJwtToken(UserSession userSession) {
        // TODO Auto-generated method stub

        HashMap<String, Object> claims = new HashMap<String, Object>();

        claims.put("userId", userSession.getUserId().toString());
        claims.put("applicationCode", userSession.getApplicationCode());
        UserDetails details = new org.springframework.security.core.userdetails.User(userSession.getUserId().toString(),
                "", Collections.emptyList());
        String token = jwtTokenProvider.generateToken(claims, details);
        return token;
    }

    /**
     * Generates a random 4-digit OTP (One-Time Password) with the first digit being
     * non-zero.
     *
     * @return A 4-digit OTP as a string.
     */

    public static String generateNonZeroRandomNumber(int length) {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        // Generate the first digit (1-9)
        otp.append(random.nextInt(9) + 1);

        // Generate the remaining three digits (0-9)
        for (int i = 1; i < length; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }

    public String verifyOTP(OTPVerificationRequest otpVerificationRequest) {
        UserSession userSession = userSessionRepository
                .findById(new ObjectId(otpVerificationRequest.sessionId()))
                .orElseThrow(() -> new UserNotFoundException("User session not found"));

        if (userSession.getStatus() == UserSessionStatus.OTP_EXPIRED ||
                userSession.getStatus() == UserSessionStatus.OTP_EXPIRED_FOR_RESEND ||
                userSession.getStatus() == UserSessionStatus.OTP_VERIFIED) {
            // OTP is expired or regenerated, throw an error
            throw new IllegalArgumentException("Invalid Session");
        }
        if (userSession.getOtp().equals(otpVerificationRequest.otp())) {
            // OTP is valid, update the user session status
            userSession.setStatus(UserSessionStatus.OTP_VERIFIED);
            userSession.setUpdatedAt(new java.util.Date());
            userSessionRepository.save(userSession);
            String token = generateJwtToken(userSession);
            return token;
        } else {
            // OTP is invalid
            throw new IllegalArgumentException("Invalid credentials");
        }
    }

    /**
     * getUserById helps to get the user by id
     * 
     * @param credential
     * @return -- User object corresponding to this credcential
     */

    public User getUserById(String userId) {
        User user = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        if (user == null) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        return user;
    }
}