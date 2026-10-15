package com.guvi.frauddetection.exception;

/**
 * Checked exception indicating an error in the JDBC or database access layer.
 * Enforces explicit exception handling in DAO methods and callers.
 */
public class DataAccessException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a DataAccessException with an informative message.
     *
     * @param message failure explanation
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * Constructs a DataAccessException with a message and the underlying root cause.
     *
     * @param message failure explanation
     * @param cause   underlying {@link Throwable} cause
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
