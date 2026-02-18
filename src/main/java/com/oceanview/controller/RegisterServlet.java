package com.oceanview.controller;

import com.google.gson.Gson;
import com.oceanview.exception.ValidationException;
import com.oceanview.model.User;
import com.oceanview.service.AuthService;
import com.oceanview.util.RBACUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles user registration (Admin only)
 */
@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(RegisterServlet.class.getName());
    private final Gson gson = new Gson();
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Only Admin can create/manage user accounts
        if (!RBACUtil.requireAuthAndRole(request, response, RBACUtil.ROLE_ADMIN)) {
            return;
        }

        Map<String, Object> jsonResponse = new HashMap<>();

        try {
            // Get form parameters
            String fullName = request.getParameter("fullName");
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            String role = request.getParameter("role");

            // Validate parameters
            if (fullName == null || fullName.trim().isEmpty()) {
                throw new ValidationException("Full name is required");
            }

            if (username == null || username.trim().isEmpty()) {
                throw new ValidationException("Username is required");
            }

            if (password == null || password.isEmpty()) {
                throw new ValidationException("Password is required");
            }

            if (confirmPassword == null || !password.equals(confirmPassword)) {
                throw new ValidationException("Passwords do not match");
            }

            if (role == null || role.trim().isEmpty()) {
                throw new ValidationException("Role is required");
            }

            // Validate role is not ADMIN
            if ("ADMIN".equalsIgnoreCase(role.trim())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Cannot register as Admin. Only Manager and Staff roles are allowed.");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Validate role is MANAGER or STAFF
            String normalizedRole = role.trim().toUpperCase();
            if (!normalizedRole.equals("MANAGER") && !normalizedRole.equals("STAFF")) {
                throw new ValidationException("Invalid role. Must be MANAGER or STAFF");
            }

            // Additional validations
            if (username.trim().length() < 3) {
                throw new ValidationException("Username must be at least 3 characters long");
            }

            if (password.length() < 6) {
                throw new ValidationException("Password must be at least 6 characters long");
            }

            if (fullName.trim().length() < 2) {
                throw new ValidationException("Full name must be at least 2 characters long");
            }

            // Attempt to register user
            User newUser = authService.registerUser(
                username.trim(), 
                password, 
                normalizedRole, 
                fullName.trim()
            );

            // Success response
            response.setStatus(HttpServletResponse.SC_CREATED);
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Registration successful! You can now login with your credentials.");
            jsonResponse.put("username", newUser.getUsername());
            jsonResponse.put("role", newUser.getRole());

        } catch (ValidationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("success", false);
            jsonResponse.put("message", e.getMessage());

        } catch (IllegalStateException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            jsonResponse.put("success", false);
            jsonResponse.put("message", e.getMessage());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "An error occurred during registration. Please try again.");
            logger.log(Level.SEVERE, "Registration error", e);
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Only Admin can view user list
        if (!RBACUtil.requireAuthAndRole(request, response, RBACUtil.ROLE_ADMIN)) {
            return;
        }

        Map<String, Object> jsonResponse = new HashMap<>();
        try {
            List<User> users = authService.getAllUsers();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(u.getId())
                  .append(",\"username\":\"").append(u.getUsername()).append("\"")
                  .append(",\"role\":\"").append(u.getRole()).append("\"}");
            }
            sb.append("]");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(sb.toString());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching users", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error fetching user list.");
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }
}
