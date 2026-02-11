package com.oceanview.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Authentication Filter - Intercepts protected URLs and enforces authentication
 * 
 * This filter checks if a user has a valid session before allowing access to protected resources.
 * If no session exists, the user is redirected to the login page.
 * 
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {
    "/pages/*",           // Protect all HTML pages
    "/api/reservations",  // Protect reservation endpoints
    "/api/reservations/*",
    "/api/rooms",         // Protect room endpoints
    "/api/rooms/*",
    "/api/reports",       // Protect report endpoints
    "/api/reports/*",
    "/api/invoice",       // Protect invoice endpoints
    "/api/invoice/*",
    "/api/dashboard"      // Protect dashboard endpoint
})
public class AuthenticationFilter implements Filter {
    
    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());
    
    // Public resources that don't require authentication
    private static final String[] PUBLIC_URLS = {
        "/login.html",
        "/register.html",
        "/index.html",
        "/index.jsp",
        "/api/login",
        "/api/register"
    };
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("AuthenticationFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());
        
        // Log the request
        LOGGER.fine("AuthenticationFilter: Processing request for: " + path);
        
        // Check if the resource is public
        if (isPublicResource(path)) {
            LOGGER.fine("Public resource accessed: " + path);
            chain.doFilter(request, response);
            return;
        }
        
        // Allow static resources (CSS, JS, images)
        if (isStaticResource(path)) {
            LOGGER.fine("Static resource accessed: " + path);
            chain.doFilter(request, response);
            return;
        }
        
        // Check for valid session
        HttpSession session = httpRequest.getSession(false);
        boolean isAuthenticated = (session != null && session.getAttribute("user") != null);
        
        if (isAuthenticated) {
            // User is authenticated, allow access
            LOGGER.fine("Authenticated user accessing: " + path);
            chain.doFilter(request, response);
        } else {
            // User is not authenticated
            LOGGER.warning("Unauthenticated access attempt to: " + path);
            
            // Check if it's an API request or page request
            if (path.startsWith("/api/")) {
                // For API requests, return JSON error response
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.getWriter().write(
                    "{\"success\": false, \"message\": \"Unauthorized - Please login first\"}"
                );
            } else {
                // For page requests, redirect to login page
                String loginPage = contextPath + "/pages/login.html";
                LOGGER.info("Redirecting to login page: " + loginPage);
                httpResponse.sendRedirect(loginPage);
            }
        }
    }
    
    private boolean isPublicResource(String path) {
        for (String publicUrl : PUBLIC_URLS) {
            if (path.equals(publicUrl) || path.endsWith(publicUrl)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isStaticResource(String path) {
        return path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") || 
               path.startsWith("/img/") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".jpeg") ||
               path.endsWith(".gif") ||
               path.endsWith(".ico");
    }
}
