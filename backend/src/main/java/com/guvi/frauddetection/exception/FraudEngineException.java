package com.guvi.frauddetection.exception;

/**
 * Checked exception thrown when an error occurs during fraud evaluation or algorithm execution.
 */
public class FraudEngineException extends Exception {

    private static final long serialVersionUID = 1L;

    public FraudEngineException(String message) {
        super(message);
    }

    public FraudEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
