package com.nayonikaeyecare.api.dto.user;

/**
 * AuthenticationRequest represents the request body for authentication.
 * 
 * @param applicationId the ID of the application making the request
 * @param credential    the credential used for authentication
 * @param secret        the secret key used for authentication
 * @param preferredLanguage the preferred language of the user, has to follow the ISO 3166 for country codes  ISO 639 language code reference
 */
public record AuthenticationRequest(String applicationCode, String credential, String secret,String preferredLanguage) {

        public AuthenticationRequest {
                if (applicationCode == null || applicationCode.isEmpty()) {
                        throw new IllegalArgumentException("Application ID cannot be null or empty");
                }
                if (credential == null || credential.isEmpty()) {
                        throw new IllegalArgumentException("Credential cannot be null or empty");
                }
        }
}