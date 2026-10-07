package com.framework.exceptions;

/**
 * Raised when a backend API call used for test setup, cleanup or verification fails unexpectedly.
 */
public class ApiException extends FrameworkException {

    private static final long serialVersionUID = 1L;

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
