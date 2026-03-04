package com.oceanview.controller;

import com.oceanview.exception.AuthenticationException;
import com.oceanview.factory.ServiceFactory;
import com.oceanview.model.User;
import com.oceanview.service.AuthService;
import com.oceanview.util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "LoginServlet", urlPatterns = {"/api/login"})
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerUtil.getLogger(LoginServlet.class);
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        super.init();
        authService = ServiceFactory.getInstance().getAuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        try {
            if (session != null && session.getAttribute("user") != null) {
                User user = (User) session.getAttribute("user");

                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{");
                out.print("\"success\": true,");
                out.print("\"loggedIn\": true,");
                out.print("\"user\": {");
                out.print("\"id\": " + user.getId() + ",");
                out.print("\"username\": \"" + escapeJson(user.getUsername()) + "\",");
                out.print("\"role\": \"" + escapeJson(user.getRole()) + "\"");
                out.print("}");
                out.print("}");
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{");
                out.print("\"success\": true,");
                out.print("\"loggedIn\": false");
                out.print("}");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking login status", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{");
            out.print("\"success\": false,");
            out.print("\"message\": \"Error checking login status\"");
            out.print("}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {

            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{");
                out.print("\"success\": false,");
                out.print("\"message\": \"Username and password are required\"");
                out.print("}");
                return;
            }

            User user = authService.login(username.trim(), password);
            String ipAddress = getClientIpAddress(request);
            LoggerUtil.logLoginSuccess(username, ipAddress);

            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setMaxInactiveInterval(30 * 60);

            response.setStatus(HttpServletResponse.SC_OK);
            out.print("{");
            out.print("\"success\": true,");
            out.print("\"message\": \"Login successful\",");
            out.print("\"user\": {");
            out.print("\"id\": " + user.getId() + ",");
            out.print("\"username\": \"" + escapeJson(user.getUsername()) + "\",");
            out.print("\"role\": \"" + escapeJson(user.getRole()) + "\"");
            out.print("}");
            out.print("}");

        } catch (AuthenticationException e) {
            String ipAddress = getClientIpAddress(request);
            String username = request.getParameter("username");
            LoggerUtil.logLoginFailure(username, ipAddress, e.getMessage());

            LOGGER.log(Level.WARNING, "Authentication failed: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{");
            out.print("\"success\": false,");
            out.print("\"message\": \"" + escapeJson(e.getMessage()) + "\"");
            out.print("}");

        } catch (Exception e) {
            String username = request.getParameter("username");
            LoggerUtil.logAuthenticationError(username, e);

            LOGGER.log(Level.SEVERE, "Error during login", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{");
            out.print("\"success\": false,");
            out.print("\"message\": \"An error occurred during login\"");
            out.print("}");
        } finally {
            out.flush();
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
