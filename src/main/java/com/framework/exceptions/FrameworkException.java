package com.framework.exceptions;

/**
 * Base unchecked exception for failures raised by the automation framework itself
 * (as opposed to assertion failures, which signal a product defect).
 */
public class FrameworkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
