package com.oceanview.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Error Response Model
 * Structured JSON response for errors
 * 
 */
public class ErrorResponse {
    
    private boolean success;
    private String errorCode;
    private String message;
    private int status;
    private String path;
    private String timestamp;
    private List<String> errors;
    private String stackTrace;
    
    /**
     * Default constructor
     */
    public ErrorResponse() {
        this.success = false;
        this.timestamp = LocalDateTime.now().toString();
        this.errors = new ArrayList<>();
    }
    
    /**
     * Constructor with basic error information
     * 
     * @param errorCode Error code
     * @param message Error message
     * @param status HTTP status code
     */
    public ErrorResponse(String errorCode, String message, int status) {
        this();
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
    }
    
    /**
     * Constructor with full error information
     * 
     * @param errorCode Error code
     * @param message Error message
     * @param status HTTP status code
     * @param path Request path
     */
    public ErrorResponse(String errorCode, String message, int status, String path) {
        this(errorCode, message, status);
        this.path = path;
    }
    
    // Getters and Setters
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
    
    public String getStackTrace() {
        return stackTrace;
    }
    
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
    
    /**
     * Builder pattern for ErrorResponse
     */
    public static class Builder {
        private ErrorResponse response;
        
        public Builder() {
            response = new ErrorResponse();
        }
        
        public Builder errorCode(String errorCode) {
            response.setErrorCode(errorCode);
            return this;
        }
        
        public Builder message(String message) {
            response.setMessage(message);
            return this;
        }
        
        public Builder status(int status) {
            response.setStatus(status);
            return this;
        }
        
        public Builder path(String path) {
            response.setPath(path);
            return this;
        }
        
        public Builder errors(List<String> errors) {
            response.setErrors(errors);
            return this;
        }
        
        public Builder addError(String error) {
            response.addError(error);
            return this;
        }
        
        public Builder stackTrace(String stackTrace) {
            response.setStackTrace(stackTrace);
            return this;
        }
        
        public ErrorResponse build() {
            return response;
        }
    }
}
