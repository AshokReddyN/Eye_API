package com.nayonikaeyecare.api.dto.user;

public record UserRequest(String phoneNumber, String firstName, String lastName, String email, String city,
        String state) {
    public UserRequest {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (firstName == null || firstName.isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (lastName == null || lastName.isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
    }
}