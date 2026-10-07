package com.framework.exceptions;

/**
 * Raised when configuration is missing or invalid. These failures are never retried,
 * because re-running a mis-configured test cannot change the outcome.
 */
public class ConfigurationException extends FrameworkException {

    private static final long serialVersionUID = 1L;

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
