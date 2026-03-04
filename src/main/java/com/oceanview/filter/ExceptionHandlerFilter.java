package com.oceanview.filter;

import com.oceanview.exception.*;
import com.oceanview.model.ErrorResponse;
import com.oceanview.util.LoggerUtil;
import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@WebFilter(filterName = "ExceptionHandlerFilter", urlPatterns = {"/api/*"})
public class ExceptionHandlerFilter implements Filter {

    private static final Gson gson = new Gson();
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LoggerUtil.logSystemEvent("ExceptionHandlerFilter initialized",
            java.util.logging.Level.INFO);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {

            chain.doFilter(request, response);

        } catch (ServletException e) {

            Throwable rootCause = e.getRootCause();

            if (rootCause instanceof BaseException) {
                handleBaseException(httpRequest, httpResponse, (BaseException) rootCause);
            } else {
                handleGenericException(httpRequest, httpResponse, e);
            }

        } catch (Exception e) {

            handleGenericException(httpRequest, httpResponse, e);
        }
    }

    private void handleBaseException(HttpServletRequest request, HttpServletResponse response,
                                     BaseException exception) throws IOException {

        String requestPath = request.getRequestURI();

        LoggerUtil.logApplicationError(
            ExceptionHandlerFilter.class.getSimpleName(),
            "Exception at " + requestPath,
            new Exception(exception.getMessage(), exception)
        );

        ErrorResponse errorResponse = new ErrorResponse.Builder()
            .errorCode(exception.getErrorCode())
            .message(exception.getMessage())
            .status(exception.getHttpStatus())
            .path(requestPath)
            .build();

        if (exception instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) exception;
            if (validationEx.hasValidationErrors()) {
                errorResponse.setErrors(validationEx.getValidationErrors());
            }
        }

        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTraceAsString(exception));
        }

        sendJsonErrorResponse(response, errorResponse, exception.getHttpStatus());
    }

    private void handleGenericException(HttpServletRequest request, HttpServletResponse response,
                                       Exception exception) throws IOException {

        String requestPath = request.getRequestURI();

        LoggerUtil.logApplicationError(
            ExceptionHandlerFilter.class.getSimpleName(),
            "Unexpected exception at " + requestPath,
            exception
        );

        ErrorResponse errorResponse = new ErrorResponse.Builder()
            .errorCode("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred. Please try again later.")
            .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            .path(requestPath)
            .build();

        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTraceAsString(exception));
            errorResponse.addError(exception.getClass().getName() + ": " + exception.getMessage());
        }

        sendJsonErrorResponse(response, errorResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private void sendJsonErrorResponse(HttpServletResponse response, ErrorResponse errorResponse,
                                       int statusCode) throws IOException {

        response.setStatus(statusCode);
        response.setContentType(CONTENT_TYPE_JSON);
        response.setCharacterEncoding(CHARACTER_ENCODING);

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        String jsonResponse = gson.toJson(errorResponse);
        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse);
        writer.flush();
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private boolean isDevMode() {
        String env = System.getProperty("app.environment", "production");
        return "development".equalsIgnoreCase(env) || "dev".equalsIgnoreCase(env);
    }

}
