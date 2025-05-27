package com.nayonikaeyecare.api.exceptions;

public class ResourceMissingException extends RuntimeException {
    public ResourceMissingException(String message) {
        super(message);
    }
}

