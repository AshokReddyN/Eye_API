package com.nayonikaeyecare.api.dto.user;

public record OTPVerificationRequest(String sessionId, String otp) {
    public OTPVerificationRequest {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (otp == null || otp.isEmpty()) {
            throw new IllegalArgumentException("OTP cannot be null or empty");
        }
    }
}