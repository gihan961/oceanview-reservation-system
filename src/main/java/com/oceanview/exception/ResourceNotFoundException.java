package com.oceanview.exception;

/**
 * Resource Not Found Exception
 * Thrown when requested resource does not exist
 * 
 */
public class ResourceNotFoundException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor with message
     * 
     * @param message Error message
     */
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", 404);
    }
    
    /**
     * Constructor with resource type and id
     * 
     * @param resourceType Type of resource (e.g., "Reservation", "Room")
     * @param resourceId ID of the resource
     */
    public ResourceNotFoundException(String resourceType, Long resourceId) {
        super(resourceType + " with ID " + resourceId + " not found", "RESOURCE_NOT_FOUND", 404);
    }
    
    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Throwable cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, "RESOURCE_NOT_FOUND", 404);
    }
}
