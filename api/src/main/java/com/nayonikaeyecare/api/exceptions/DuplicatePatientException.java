package com.nayonikaeyecare.api.exceptions;

public class DuplicatePatientException extends RuntimeException {
    public DuplicatePatientException(String message) {
        super(message);
    }
}