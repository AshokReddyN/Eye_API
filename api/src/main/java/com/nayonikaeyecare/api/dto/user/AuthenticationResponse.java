package com.nayonikaeyecare.api.dto.user;

/**
 * AuthenticationResponse represents the response body for authentication.
 * 
 * @param token the JWT token generated after successful authentication
 */

public record AuthenticationResponse(
        String sessionId, boolean newUserRegistered, String userId) {
}