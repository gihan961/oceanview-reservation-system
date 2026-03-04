package com.oceanview.exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends BaseException {

    private static final long serialVersionUID = 1L;
    private final List<String> validationErrors;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
        this.validationErrors = new ArrayList<>();
    }

    public ValidationException(String message, List<String> validationErrors) {
        super(message, "VALIDATION_ERROR", 400);
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause, "VALIDATION_ERROR", 400);
        this.validationErrors = new ArrayList<>();
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }
}
