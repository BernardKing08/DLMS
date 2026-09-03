package com.dlms.frontend.exception;

/**
 * Carries a backend 400 (bad credentials, failed validation, duplicate
 * email, etc.) back up to the controller so it can re-render the originating
 * form with the message, instead of falling through to a generic error page.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
