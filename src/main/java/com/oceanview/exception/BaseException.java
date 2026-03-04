package com.oceanview.exception;

public abstract class BaseException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final int httpStatus;

    public BaseException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public BaseException(String message, Throwable cause, String errorCode, int httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
