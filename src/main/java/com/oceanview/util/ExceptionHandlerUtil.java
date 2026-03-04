package com.oceanview.util;

import com.oceanview.exception.*;
import com.oceanview.model.ErrorResponse;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ExceptionHandlerUtil {

    private static final Gson gson = new Gson();

    public static void sendErrorResponse(HttpServletResponse response, ErrorResponse errorResponse)
            throws IOException {

        response.setStatus(errorResponse.getStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        PrintWriter writer = response.getWriter();
        writer.write(gson.toJson(errorResponse));
        writer.flush();
    }

    public static ErrorResponse createErrorResponse(BaseException exception, String path) {
        ErrorResponse.Builder builder = new ErrorResponse.Builder()
            .errorCode(exception.getErrorCode())
            .message(exception.getMessage())
            .status(exception.getHttpStatus())
            .path(path);

        if (exception instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) exception;
            if (validationEx.hasValidationErrors()) {
                builder.errors(validationEx.getValidationErrors());
            }
        }

        return builder.build();
    }

    public static ErrorResponse createAuthenticationError(String message, String path) {
        return new ErrorResponse.Builder()
            .errorCode("AUTH_ERROR")
            .message(message)
            .status(HttpServletResponse.SC_UNAUTHORIZED)
            .path(path)
            .build();
    }

    public static ErrorResponse createAuthorizationError(String message, String path) {
        return new ErrorResponse.Builder()
            .errorCode("AUTHORIZATION_ERROR")
            .message(message)
            .status(HttpServletResponse.SC_FORBIDDEN)
            .path(path)
            .build();
    }

    public static ErrorResponse createValidationError(String message,
                                                     java.util.List<String> errors,
                                                     String path) {
        return new ErrorResponse.Builder()
            .errorCode("VALIDATION_ERROR")
            .message(message)
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .errors(errors)
            .path(path)
            .build();
    }

    public static ErrorResponse createNotFoundError(String message, String path) {
        return new ErrorResponse.Builder()
            .errorCode("RESOURCE_NOT_FOUND")
            .message(message)
            .status(HttpServletResponse.SC_NOT_FOUND)
            .path(path)
            .build();
    }

    public static ErrorResponse createInternalServerError(String message, String path) {
        return new ErrorResponse.Builder()
            .errorCode("INTERNAL_SERVER_ERROR")
            .message(message)
            .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            .path(path)
            .build();
    }

    public static ErrorResponse createBadRequestError(String message, String path) {
        return new ErrorResponse.Builder()
            .errorCode("BAD_REQUEST")
            .message(message)
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .path(path)
            .build();
    }

    public static ErrorResponse handleException(Exception exception, String path, String component) {

        if (exception instanceof BaseException) {
            BaseException baseEx = (BaseException) exception;
            LoggerUtil.logApplicationError(component, path, exception);
            return createErrorResponse(baseEx, path);

        } else {
            LoggerUtil.logApplicationError(component, "Unexpected error at " + path, exception);
            return createInternalServerError("An unexpected error occurred", path);
        }
    }
}
