package com.oceanview.exception;

public class ResourceNotFoundException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", 404);
    }

    public ResourceNotFoundException(String resourceType, Long resourceId) {
        super(resourceType + " with ID " + resourceId + " not found", "RESOURCE_NOT_FOUND", 404);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, "RESOURCE_NOT_FOUND", 404);
    }
}
