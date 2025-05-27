package com.nayonikaeyecare.api.entities.user;

/**
 * UserStatus represents the status of a user in the system.
 * It can be used to track the current state of a user account.
 * The possible statuses are:
 * - ACTIVE: The user account is active and can log in.
 * - INACTIVE: The user account is inactive and cannot log in.
 * - SUSPENDED: The user account is suspended and cannot log in.
 * - DELETED: The user account is deleted and cannot log in.    
 * * @author Jayasimha Prasad
 * 
  * @since 1.0
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    DELETED
}