package com.guvi.frauddetection.exception;

/**
 * Unchecked exception thrown when a fraud detection rule fails during execution.
 */
public class RuleExecutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RuleExecutionException(String message) {
        super(message);
    }

    public RuleExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
