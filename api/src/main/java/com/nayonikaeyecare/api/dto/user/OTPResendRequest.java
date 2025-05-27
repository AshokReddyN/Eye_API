
package com.nayonikaeyecare.api.dto.user;

public record OTPResendRequest(String sessionId) {
    public OTPResendRequest {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
    }

}