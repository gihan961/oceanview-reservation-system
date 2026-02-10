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

/**
 * Global Exception Handler Filter
 * Centralized exception handling for all requests
 * Returns structured JSON error responses
 * 
 */
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
            // Continue with the request
            chain.doFilter(request, response);
            
        } catch (ServletException e) {
            // Unwrap ServletException to check for custom exceptions
            Throwable rootCause = e.getRootCause();
            
            if (rootCause instanceof BaseException) {
                handleBaseException(httpRequest, httpResponse, (BaseException) rootCause);
            } else {
                handleGenericException(httpRequest, httpResponse, e);
            }
            
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            handleGenericException(httpRequest, httpResponse, e);
        }
    }
    
    /**
     * Handle custom BaseException and its subclasses
     */
    private void handleBaseException(HttpServletRequest request, HttpServletResponse response, 
                                     BaseException exception) throws IOException {
        
        String requestPath = request.getRequestURI();
        
        // Log the exception
        LoggerUtil.logApplicationError(
            ExceptionHandlerFilter.class.getSimpleName(),
            "Exception at " + requestPath,
            new Exception(exception.getMessage(), exception)
        );
        
        // Build error response
        ErrorResponse errorResponse = new ErrorResponse.Builder()
            .errorCode(exception.getErrorCode())
            .message(exception.getMessage())
            .status(exception.getHttpStatus())
            .path(requestPath)
            .build();
        
        // Add validation errors if ValidationException
        if (exception instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) exception;
            if (validationEx.hasValidationErrors()) {
                errorResponse.setErrors(validationEx.getValidationErrors());
            }
        }
        
        // Add stack trace in development mode (optional)
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTraceAsString(exception));
        }
        
        // Send JSON response
        sendJsonErrorResponse(response, errorResponse, exception.getHttpStatus());
    }
    
    /**
     * Handle generic exceptions (unexpected errors)
     */
    private void handleGenericException(HttpServletRequest request, HttpServletResponse response, 
                                       Exception exception) throws IOException {
        
        String requestPath = request.getRequestURI();
        
        // Log the exception
        LoggerUtil.logApplicationError(
            ExceptionHandlerFilter.class.getSimpleName(),
            "Unexpected exception at " + requestPath,
            exception
        );
        
        // Build error response for internal server error
        ErrorResponse errorResponse = new ErrorResponse.Builder()
            .errorCode("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred. Please try again later.")
            .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            .path(requestPath)
            .build();
        
        // Add detailed error in development mode
        if (isDevMode()) {
            errorResponse.setStackTrace(getStackTraceAsString(exception));
            errorResponse.addError(exception.getClass().getName() + ": " + exception.getMessage());
        }
        
        // Send JSON response
        sendJsonErrorResponse(response, errorResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Send JSON error response
     */
    private void sendJsonErrorResponse(HttpServletResponse response, ErrorResponse errorResponse, 
                                       int statusCode) throws IOException {
        
        response.setStatus(statusCode);
        response.setContentType(CONTENT_TYPE_JSON);
        response.setCharacterEncoding(CHARACTER_ENCODING);
        
        // Prevent caching of error responses
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Write JSON response
        String jsonResponse = gson.toJson(errorResponse);
        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse);
        writer.flush();
    }
    
    /**
     * Get stack trace as string
     */
    private String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Check if running in development mode
     * Can be configured via system property or environment variable
     */
    private boolean isDevMode() {
        String env = System.getProperty("app.environment", "production");
        return "development".equalsIgnoreCase(env) || "dev".equalsIgnoreCase(env);
    }
    
}
