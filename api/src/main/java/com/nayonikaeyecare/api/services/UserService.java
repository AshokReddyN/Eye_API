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
import com.nayonikaeyecare.api.repositories.user.UserNotFoundException;
import com.nayonikaeyecare.api.repositories.user.UserRepository;
import com.nayonikaeyecare.api.repositories.user.UserSessionRepository;
import com.nayonikaeyecare.api.security.JWTTokenProvider;
import java.util.Date;

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
        UserCredential userCredential = userCredentialRepository.findByCredential(authenticationRequest.credential());
        boolean userCreated = false;
        if (userCredential == null) {
            // check if the user can be created for the application because of
            // autoregistration
            userCreated = checkAndCreateUserForApplication(authenticationRequest, userCredential);
            userCredential = userCredentialRepository
                    .findByCredential(authenticationRequest.credential());
        } else {
            // user crednetial already exists hence no need to create a new user
            userCreated = userRepository
            .findByUserCredentialId(userCredential.getId()).getPhoneNumber()==null;
        }
        // check if the user credential still not null because of the application auto
        // registration rules is not null
        if (userCredential != null) {
            String otp = generateOtp();
            // generate a new session for the user and send the back
            User user = userRepository
            .findByUserCredentialId(userCredential.getId());
            UserSession newUserSession = UserSession.builder()
                    .userId(user.getId())
                    .applicationCode(authenticationRequest.applicationCode())
                    .otp(otp)
                    .status(UserSessionStatus.INITIATIED)
                    .createdAt(new java.util.Date())
                    .build();
            UserSession savedUserSession = userSessionRepository
                    .save(newUserSession);

            return new AuthenticationResponse(savedUserSession.getId().toString(),
                    userCreated, user.getId().toString());
        } else {
            // throw an error as the application does not allow for auto registration
            throw new UserNotFoundException("Invalid credentials");
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
            UserCredential userCredential) {
        Application application = applicationRepository
                .findByCode(authenticationRequest.applicationCode());
        boolean userCreated = false;
        if (application != null) {
            if (application.isAllowAutoRegistration()) {
                // User does not exist, but auto-registration is allowed
                // auto create a new user credential
                userCredential = UserCredential.builder()
                        .credential(authenticationRequest.credential())
                        .build();
                userCredentialRepository.save(userCredential);
                // Create a new user with the provided details
                User user = User.builder()
                        .userCredentialId(userCredential.getId())
                        .createdAt(new java.util.Date())
                        .build();
                userRepository.save(user);
                userCreated = true;
                return userCreated;

            } else {
                // application does not allow for automatic creation of user credentials
                throw new IllegalArgumentException("Invalid credentials");
            }
        } else {
            // application code was invalid
            throw new IllegalArgumentException("Incorrect credentials");
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