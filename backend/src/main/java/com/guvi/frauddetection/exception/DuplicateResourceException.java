package com.guvi.frauddetection.exception;

/**
 * Checked exception thrown when attempting to insert an entity that already exists (e.g. duplicate email).
 */
public class DuplicateResourceException extends Exception {

    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
