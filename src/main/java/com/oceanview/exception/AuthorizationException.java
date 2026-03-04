package com.oceanview.exception;

public class AuthorizationException extends BaseException {

    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super(message, "AUTHORIZATION_ERROR", 403);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause, "AUTHORIZATION_ERROR", 403);
    }

    public AuthorizationException(String message, String errorCode) {
        super(message, errorCode, 403);
    }
}
