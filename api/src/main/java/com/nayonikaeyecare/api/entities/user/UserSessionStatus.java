package com.nayonikaeyecare.api.entities.user;

/**
 * UserSessionStatus represents the status of a user session.
 * It can be used to track the current state of a user session.
 * The possible statuses are:
 * - INITIATED: The user session has been initiated.
 * - OTP_GENERATED: The OTP has been generated for the user session.
 * - OTP_VERIFIED: The OTP has been verified for the user session.
 * - OTP_EXPIRED: The OTP has expired for the user session.
 * - OTP_RESENT: The OTP has been resent for the user session.
 */
public enum UserSessionStatus {
    INITIATIED,
    OTP_GENERATED,
    OTP_VERIFIED,
    OTP_EXPIRED,
    // this otp has been set to expeire becase a resend reqeust has been made
    OTP_EXPIRED_FOR_RESEND,
    OTP_REGENERATED
}