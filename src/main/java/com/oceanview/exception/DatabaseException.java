package com.oceanview.exception;

/**
 * Database Exception
 * Thrown when database operations fail
 * 
 */
public class DatabaseException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor with message
     * 
     * @param message Error message
     */
    public DatabaseException(String message) {
        super(message, "DATABASE_ERROR", 500);
    }
    
    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Throwable cause
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause, "DATABASE_ERROR", 500);
    }
    
    /**
     * Constructor with custom error code
     * 
     * @param message Error message
     * @param errorCode Custom error code
     */
    public DatabaseException(String message, String errorCode) {
        super(message, errorCode, 500);
    }
}
