package com.oceanview.exception;

/**
 * Business Logic Exception
 * Thrown when business rules are violated
 * 
 */
public class BusinessLogicException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor with message
     * 
     * @param message Error message
     */
    public BusinessLogicException(String message) {
        super(message, "BUSINESS_LOGIC_ERROR", 422);
    }
    
    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Throwable cause
     */
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause, "BUSINESS_LOGIC_ERROR", 422);
    }
    
    /**
     * Constructor with custom error code
     * 
     * @param message Error message
     * @param errorCode Custom error code
     */
    public BusinessLogicException(String message, String errorCode) {
        super(message, errorCode, 422);
    }
}
