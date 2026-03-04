package com.oceanview.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {
    "/pages/*",
    "/api/reservations",
    "/api/reservations/*",
    "/api/rooms",
    "/api/rooms/*",
    "/api/reports",
    "/api/reports/*",
    "/api/invoice",
    "/api/invoice/*",
    "/api/dashboard"
})
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());

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

        LOGGER.fine("AuthenticationFilter: Processing request for: " + path);

        if (isPublicResource(path)) {
            LOGGER.fine("Public resource accessed: " + path);
            chain.doFilter(request, response);
            return;
        }

        if (isStaticResource(path)) {
            LOGGER.fine("Static resource accessed: " + path);
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        boolean isAuthenticated = (session != null && session.getAttribute("user") != null);

        if (isAuthenticated) {

            LOGGER.fine("Authenticated user accessing: " + path);
            chain.doFilter(request, response);
        } else {

            LOGGER.warning("Unauthenticated access attempt to: " + path);

            if (path.startsWith("/api/")) {

                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.getWriter().write(
                    "{\"success\": false, \"message\": \"Unauthorized - Please login first\"}"
                );
            } else {

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
