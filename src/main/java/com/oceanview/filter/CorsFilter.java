package com.oceanview.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * CORS Filter - Handles Cross-Origin Resource Sharing (CORS) headers
 * 
 * This filter adds appropriate CORS headers to allow API access from different origins.
 * Useful for developing frontend applications separately or allowing third-party integrations.
 * 
 */
@WebFilter(filterName = "CorsFilter", urlPatterns = {"/api/*"})
public class CorsFilter implements Filter {
    
    private static final Logger LOGGER = Logger.getLogger(CorsFilter.class.getName());
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("CorsFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Set CORS headers
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", 
            "Content-Type, Authorization, X-Requested-With");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        
        // Handle preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            LOGGER.fine("CORS preflight request received");
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        // Continue with the filter chain
        chain.doFilter(request, response);
    }
    
}
