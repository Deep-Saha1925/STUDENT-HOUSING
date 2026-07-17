package com.deep.studenthousing.exception;

/**
 * Thrown when a logged-in user tries to act on a resource they don't own or
 * aren't permitted to touch (e.g. an owner viewing another owner's property
 * bookings). Caught by GlobalExceptionHandler and rendered as a friendly
 * "unauthorized" page instead of a raw stack trace.
 */
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}