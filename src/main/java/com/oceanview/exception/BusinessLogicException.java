package com.oceanview.exception;

public class BusinessLogicException extends BaseException {

    private static final long serialVersionUID = 1L;

    public BusinessLogicException(String message) {
        super(message, "BUSINESS_LOGIC_ERROR", 422);
    }

    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause, "BUSINESS_LOGIC_ERROR", 422);
    }

    public BusinessLogicException(String message, String errorCode) {
        super(message, errorCode, 422);
    }
}
