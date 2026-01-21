package com.oceanview.exception;

/**
 * Authentication Exception
 * Thrown when authentication fails
 * 
 */
public class AuthenticationException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor with message
     * 
     * @param message Error message
     */
    public AuthenticationException(String message) {
        super(message, "AUTH_ERROR", 401);
    }
    
    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Throwable cause
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, "AUTH_ERROR", 401);
    }
    
    /**
     * Constructor with custom error code
     * 
     * @param message Error message
     * @param errorCode Custom error code
     */
    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode, 401);
    }
}

