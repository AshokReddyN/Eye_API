package com.nayonikaeyecare.api.controllers.user;

import com.nayonikaeyecare.api.dto.user.*;
import com.nayonikaeyecare.api.entities.user.UserSession;
import com.nayonikaeyecare.api.services.UserService;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthenticationControllerTest {

    private UserService userService;
    private AuthenticationController authenticationController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        authenticationController = new AuthenticationController(null, null, userService, null);
    }

    @Test
    void testAuthenticateUser() {
        AuthenticationRequest request = new AuthenticationRequest("MOBILE_APP", "12454334", "", "");
        AuthenticationResponse expectedResponse = new AuthenticationResponse("sessionId", false, "userId");

        when(userService.authenticateUser(request)).thenReturn(expectedResponse);

        ResponseEntity<AuthenticationResponse> response = (ResponseEntity<AuthenticationResponse>) authenticationController
                .authenticateUser(request, false);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(userService, times(1)).authenticateUser(request);
    }

    @Test
    void testVerifyOtpSuccess() {
        OTPVerificationRequest request = new OTPVerificationRequest("sessionId", "123456");
        String token = "jwtToken";

        when(userService.verifyOTP(request)).thenReturn(token);

        ResponseEntity<?> response = authenticationController.verifyOtp(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(new OTPVerificationResponse(token), response.getBody());
        verify(userService, times(1)).verifyOTP(request);
    }

    @Test
    void testVerifyOtpFailure() {
        OTPVerificationRequest request = new OTPVerificationRequest("67fe65b3a2336d59ed95c6df", "123456");
        String errorMessage = "Invalid OTP";

        when(userService.verifyOTP(request)).thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = authenticationController.verifyOtp(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals(Collections.singletonMap("message", errorMessage), response.getBody());
        verify(userService, times(1)).verifyOTP(request);
    }

    @Test
    void testResendOtpSuccess() {
        OTPResendRequest request = new OTPResendRequest("sessionId");
        UserSession session = new UserSession();
        session.setId(new ObjectId("67fe65b3a2336d59ed95c6df"));

        when(userService.resendOTP(request)).thenReturn(session);

        ResponseEntity<?> response = authenticationController.resendOtp(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(new OTPResendResponse("1"), response.getBody());
        verify(userService, times(1)).resendOTP(request);
    }

    @Test
    void testResendOtpFailure() {
        OTPResendRequest request = new OTPResendRequest("sessionId");
        String errorMessage = "Session not found";

        when(userService.resendOTP(request)).thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = authenticationController.resendOtp(request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals(Collections.singletonMap("message", errorMessage), response.getBody());
        verify(userService, times(1)).resendOTP(request);
    }
}
