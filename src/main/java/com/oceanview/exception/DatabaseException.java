package com.oceanview.exception;

public class DatabaseException extends BaseException {

    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message, "DATABASE_ERROR", 500);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause, "DATABASE_ERROR", 500);
    }

    public DatabaseException(String message, String errorCode) {
        super(message, errorCode, 500);
    }
}
