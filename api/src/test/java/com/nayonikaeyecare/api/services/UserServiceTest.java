package com.nayonikaeyecare.api.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.HashMap;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nayonikaeyecare.api.dto.user.AuthenticationRequest;
import com.nayonikaeyecare.api.dto.user.AuthenticationResponse;
import com.nayonikaeyecare.api.dto.user.OTPResendRequest;
import com.nayonikaeyecare.api.dto.user.OTPVerificationRequest;
import com.nayonikaeyecare.api.dto.user.UserRequest;
import com.nayonikaeyecare.api.entities.user.User;
import com.nayonikaeyecare.api.entities.user.UserCredential;
import com.nayonikaeyecare.api.entities.user.UserSession;
import com.nayonikaeyecare.api.entities.user.UserSessionStatus;
import com.nayonikaeyecare.api.repositories.application.ApplicationRepository;
import com.nayonikaeyecare.api.repositories.user.UserCredentialRepository;
import com.nayonikaeyecare.api.repositories.user.UserRepository;
import com.nayonikaeyecare.api.repositories.user.UserSessionRepository;
import com.nayonikaeyecare.api.security.JWTTokenProvider;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private JWTTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser() {
        UserRequest userRequest = new UserRequest("John", "Doe", "1234567890", "testCredential", "appCode", "");
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.createUser(userRequest);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testAuthenticateUser_ValidCredentials() {
        AuthenticationRequest authRequest = new AuthenticationRequest("testCredential", "appCode", "", "");
        UserCredential userCredential = UserCredential.builder().credential("testCredential").build();
        User user = User.builder().id(new ObjectId()).build();
        UserSession userSession = UserSession.builder().id(new ObjectId()).build();

        when(userCredentialRepository.findByCredential("testCredential")).thenReturn(userCredential);
        when(userRepository.findByUserCredentialId(userCredential.getId())).thenReturn(user);
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(userSession);

        AuthenticationResponse response = userService.authenticateUser(authRequest);

        assertNotNull(response);
        verify(userSessionRepository, times(1)).save(any(UserSession.class));
    }

    @Test
    void testResendOTP_ValidSession() {
        ObjectId sessionId = new ObjectId();
        OTPResendRequest otpResendRequest = new OTPResendRequest(sessionId.toString());
        UserSession existingSession = UserSession.builder()
                .id(sessionId)
                .status(UserSessionStatus.INITIATIED)
                .build();
        UserSession newSession = UserSession.builder().id(new ObjectId()).build();

        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(newSession);

        UserSession result = userService.resendOTP(otpResendRequest);

        assertNotNull(result);
        verify(userSessionRepository, times(2)).save(any(UserSession.class));
    }

    @Test
    void testVerifyOTP_ValidOTP() {
        ObjectId sessionId = new ObjectId();
        OTPVerificationRequest otpVerificationRequest = new OTPVerificationRequest(sessionId.toString(), "1234");
        UserSession userSession = UserSession.builder()
                .id(sessionId)
                .otp("1234")
                .status(UserSessionStatus.INITIATIED)
                .build();

        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(userSession));
        when(jwtTokenProvider.generateToken((new HashMap<>()), any())).thenReturn("jwtToken");

        String token = userService.verifyOTP(otpVerificationRequest);

        assertNotNull(token);
        assertEquals("jwtToken", token);
        verify(userSessionRepository, times(1)).save(userSession);
    }

    @Test
    void testVerifyOTP_InvalidOTP() {
        ObjectId sessionId = new ObjectId();
        OTPVerificationRequest otpVerificationRequest = new OTPVerificationRequest(sessionId.toString(), "5678");
        UserSession userSession = UserSession.builder()
                .id(sessionId)
                .otp("1234")
                .status(UserSessionStatus.INITIATIED)
                .build();

        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(userSession));

        assertThrows(IllegalArgumentException.class, () -> userService.verifyOTP(otpVerificationRequest));
    }
}
