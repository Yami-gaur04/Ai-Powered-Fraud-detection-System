package com.guvi.frauddetection.exception;

/**
 * Unchecked exception thrown when transaction payload or domain invariants are violated.
 */
public class InvalidTransactionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
