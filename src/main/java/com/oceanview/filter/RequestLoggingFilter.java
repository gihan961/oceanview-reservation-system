package com.oceanview.filter;

import com.oceanview.util.LoggerUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Request Logging Filter - Logs all HTTP requests for monitoring and debugging
 * 
 * This filter logs request details including:
 * - Request method and URI
 * - User information (if authenticated)
 * - Response status
 * - Request processing time
 * 
 */
@WebFilter(filterName = "RequestLoggingFilter", urlPatterns = {"/*"})
public class RequestLoggingFilter implements Filter {
    
    private static final Logger LOGGER = LoggerUtil.getLogger(RequestLoggingFilter.class);
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("RequestLoggingFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Record start time
        long startTime = System.currentTimeMillis();
        
        // Get request details
        String method = httpRequest.getMethod();
        String requestURI = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String remoteAddr = httpRequest.getRemoteAddr();
        
        // Get user information if authenticated
        String username = "anonymous";
        HttpSession session = httpRequest.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            username = (String) session.getAttribute("username");
        }
        
        // Build request log
        StringBuilder requestLog = new StringBuilder();
        requestLog.append("[REQUEST] ");
        requestLog.append(method).append(" ");
        requestLog.append(requestURI);
        if (queryString != null) {
            requestLog.append("?").append(queryString);
        }
        requestLog.append(" | User: ").append(username);
        requestLog.append(" | IP: ").append(remoteAddr);
        
        LOGGER.info(requestLog.toString());
        
        try {
            // Continue with the filter chain
            chain.doFilter(request, response);
            
        } finally {
            // Calculate processing time
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Get response status
            int status = httpResponse.getStatus();
            
            // Use LoggerUtil for structured logging
            LoggerUtil.logRequest(method, requestURI, username, remoteAddr, status, processingTime);
            
            // Build response log for local logger
            StringBuilder responseLog = new StringBuilder();
            responseLog.append("[RESPONSE] ");
            responseLog.append(method).append(" ");
            responseLog.append(requestURI);
            responseLog.append(" | Status: ").append(status);
            responseLog.append(" | Time: ").append(processingTime).append("ms");
            responseLog.append(" | User: ").append(username);
            
            // Log based on status code
            if (status >= 500) {
                LOGGER.severe(responseLog.toString());
            } else if (status >= 400) {
                LOGGER.warning(responseLog.toString());
            } else {
                LOGGER.info(responseLog.toString());
            }
        }
    }
    
}
