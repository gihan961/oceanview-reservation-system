package com.oceanview.exception;

public class AuthenticationException extends BaseException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message, "AUTH_ERROR", 401);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, "AUTH_ERROR", 401);
    }

    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode, 401);
    }
}
