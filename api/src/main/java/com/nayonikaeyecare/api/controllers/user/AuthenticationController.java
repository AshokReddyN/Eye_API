package com.nayonikaeyecare.api.controllers.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nayonikaeyecare.api.dto.user.AuthenticationRequest;
import com.nayonikaeyecare.api.dto.user.AuthenticationResponse;
import com.nayonikaeyecare.api.dto.user.OTPResendRequest;
import com.nayonikaeyecare.api.dto.user.OTPResendResponse;
import com.nayonikaeyecare.api.dto.user.OTPVerificationRequest;
import com.nayonikaeyecare.api.dto.user.OTPVerificationResponse;
import com.nayonikaeyecare.api.dto.user.UserRequest;
import com.nayonikaeyecare.api.entities.user.User;
import com.nayonikaeyecare.api.entities.user.UserSession;
import com.nayonikaeyecare.api.repositories.user.UserNotFoundException;
import com.nayonikaeyecare.api.services.ApplicationUserDetailsService;
import com.nayonikaeyecare.api.services.UserService;
import com.nayonikaeyecare.api.services.VisionAmbassadorService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

 
import org.springframework.web.bind.annotation.CrossOrigin;
 
 
@CrossOrigin(origins = {"http://localhost:3000","http://nayonika-user-management-dev-1511095685.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-qa-580028363.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-stg-1382154925.ap-south-1.elb.amazonaws.com"})
 

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private UserService userService;
    private final VisionAmbassadorService visionAmbassadorService;

    public AuthenticationController(AuthenticationManager authenticationManager,
            ApplicationUserDetailsService userDetailsService,
            UserService userService, VisionAmbassadorService visionAmbassadorService) {
        this.userService = userService;
        this.visionAmbassadorService = visionAmbassadorService;
    }

    @PostMapping("/vision-ambassador-rquest-otp")
    public ResponseEntity<?> visionAmbassadorSignin(@RequestBody AuthenticationRequest request) {
        // TODO: process POST request
        AuthenticationResponse response = visionAmbassadorService.visionAmbassadorSignin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> authenticateUser(
            @RequestBody AuthenticationRequest authenticationRequest,
            @RequestParam(required = false, defaultValue = "false") boolean visionAmbassador) {
        AuthenticationResponse response = null;
        if (visionAmbassador) {
            response = visionAmbassadorService.visionAmbassadorSignin(authenticationRequest);
        } else {
            response = userService.authenticateUser(authenticationRequest);
        }
        // Map<String, String> jsonResponse = Collections.singletonMap("sessionId",
        // response.sessionId());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserRequest userRequest) {
        try {
            userService.updateUser(userId, userRequest);
            return ResponseEntity.ok("User updated successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestBody OTPVerificationRequest otpVerificationRequest) {

        try {
            final String token = userService.verifyOTP(otpVerificationRequest);

            return ResponseEntity.ok()
                    .body(new OTPVerificationResponse(token));
        } catch (Exception e) {
            Map<String, String> errorResponse = Collections.singletonMap("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody OTPResendRequest resendRequest) {
        try {
            final UserSession session = userService.resendOTP(resendRequest);

            return ResponseEntity.ok()
                    .body(new OTPResendResponse(session.getId().toString()));
        } catch (Exception e) {
            Map<String, String> errorResponse = Collections.singletonMap("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            Map<String, String> errorResponse = Collections.singletonMap("message",
                    "User not found with id: " + userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = Collections.singletonMap("message", "Invalid user ID format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}
