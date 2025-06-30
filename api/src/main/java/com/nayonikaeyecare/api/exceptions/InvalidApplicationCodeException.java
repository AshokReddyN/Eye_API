package com.nayonikaeyecare.api.exceptions;

public class InvalidApplicationCodeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidApplicationCodeException(String message) {
        super(message);
    }

    public InvalidApplicationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
 