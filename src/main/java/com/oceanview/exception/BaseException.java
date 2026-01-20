package com.oceanview.exception;

/**
 * Base Exception for all custom exceptions
 * Provides common functionality for error handling
 * 
 */
public abstract class BaseException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final int httpStatus;
    
    /**
     * Constructor with message
     * 
     * @param message Error message
     * @param errorCode Error code for identification
     * @param httpStatus HTTP status code
     */
    public BaseException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Throwable cause
     * @param errorCode Error code for identification
     * @param httpStatus HTTP status code
     */
    public BaseException(String message, Throwable cause, String errorCode, int httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    /**
     * Get error code
     * 
     * @return Error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Get HTTP status code
     * 
     * @return HTTP status code
     */
    public int getHttpStatus() {
        return httpStatus;
    }
}
