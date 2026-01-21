package com.oceanview.exception;

/**
 * Authorization Exception
 * Thrown when user lacks required permissions
 * 
 */
public class AuthorizationException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor with message
     * 
     * @param message Error message
     */
    public AuthorizationException(String message) {
        super(message, "AUTHORIZATION_ERROR", 403);
    }
    
    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Throwable cause
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause, "AUTHORIZATION_ERROR", 403);
    }
    
    /**
     * Constructor with custom error code
     * 
     * @param message Error message
     * @param errorCode Custom error code
     */
    public AuthorizationException(String message, String errorCode) {
        super(message, errorCode, 403);
    }
}
