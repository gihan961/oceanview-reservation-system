package com.oceanview.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation Exception
 * Thrown when input validation fails
 * 
 */
public class ValidationException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    private final List<String> validationErrors;
    
    /**
     * Constructor with message
     * 
     * @param message Error message
     */
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
        this.validationErrors = new ArrayList<>();
    }
    
    /**
     * Constructor with message and validation errors
     * 
     * @param message Error message
     * @param validationErrors List of validation error messages
     */
    public ValidationException(String message, List<String> validationErrors) {
        super(message, "VALIDATION_ERROR", 400);
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }
    
    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Throwable cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause, "VALIDATION_ERROR", 400);
        this.validationErrors = new ArrayList<>();
    }
    
    /**
     * Get validation errors
     * 
     * @return List of validation error messages
     */
    public List<String> getValidationErrors() {
        return validationErrors;
    }
    
    /**
     * Check if there are validation errors
     * 
     * @return true if validation errors exist
     */
    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }
}
