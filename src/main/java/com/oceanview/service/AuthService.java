package com.oceanview.service;

import com.oceanview.dao.UserDAO;
import com.oceanview.dao.impl.UserDAOImpl;
import com.oceanview.exception.AuthenticationException;
import com.oceanview.exception.ValidationException;
import com.oceanview.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User login(String username, String password)
            throws AuthenticationException, ValidationException {

        validateLoginInput(username, password);

        try {

            String passwordHash = hashPassword(password);

            User user = userDAO.authenticate(username, passwordHash);

            if (user == null) {
                LOGGER.log(Level.WARNING, "Login failed for username: {0}", username);
                throw new AuthenticationException("Invalid username or password");
            }

            LOGGER.log(Level.INFO, "User logged in successfully: {0}", username);
            return user;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during authentication", e);
            throw new AuthenticationException("Authentication failed due to system error");
        }
    }

    public User createUser(String username, String password, String role)
            throws ValidationException {

        validateUserCreation(username, password, role);

        try {

            User existingUser = userDAO.findByUsername(username);
            if (existingUser != null) {
                throw new ValidationException("Username already exists");
            }

            String passwordHash = hashPassword(password);

            User user = new User(username, passwordHash, role.toUpperCase());
            boolean created = userDAO.create(user);

            if (!created) {
                throw new ValidationException("Failed to create user");
            }

            LOGGER.log(Level.INFO, "User created successfully: {0}", username);
            return user;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during user creation", e);
            throw new ValidationException("Failed to create user due to system error");
        }
    }

    public User registerUser(String username, String password, String role, String fullName)
            throws ValidationException, IllegalStateException {

        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }

        if (role == null || role.trim().isEmpty()) {
            throw new ValidationException("Role cannot be empty");
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name cannot be empty");
        }

        if (username.trim().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long");
        }

        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }

        if (fullName.trim().length() < 2) {
            throw new ValidationException("Full name must be at least 2 characters long");
        }

        String normalizedRole = role.trim().toUpperCase();
        if (normalizedRole.equals("ADMIN")) {
            throw new ValidationException("Cannot register as Admin. Only Manager and Staff roles are allowed.");
        }

        if (!normalizedRole.equals("MANAGER") && !normalizedRole.equals("STAFF")) {
            throw new ValidationException("Invalid role. Must be MANAGER or STAFF");
        }

        try {

            User existingUser = userDAO.findByUsername(username.trim());
            if (existingUser != null) {
                LOGGER.log(Level.WARNING, "Registration failed - username already exists: {0}", username);
                throw new IllegalStateException("Username '" + username + "' is already taken. Please choose a different username.");
            }

            String passwordHash = hashPassword(password);

            User user = new User(username.trim(), passwordHash, normalizedRole);
            boolean created = userDAO.create(user);

            if (!created) {
                throw new ValidationException("Failed to register user. Please try again.");
            }

            LOGGER.log(Level.INFO, "User registered successfully: {0} with role: {1}",
                      new Object[]{username, normalizedRole});
            return user;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during registration", e);
            throw new ValidationException("Registration failed due to system error. Please try again later.");
        }
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword)
            throws AuthenticationException, ValidationException {

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new ValidationException("New password cannot be empty");
        }

        if (newPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }

        try {

            User user = userDAO.findById(userId);
            if (user == null) {
                throw new ValidationException("User not found");
            }

            String oldPasswordHash = hashPassword(oldPassword);
            if (!user.getPasswordHash().equals(oldPasswordHash)) {
                throw new AuthenticationException("Old password is incorrect");
            }

            String newPasswordHash = hashPassword(newPassword);
            user.setPasswordHash(newPasswordHash);

            boolean updated = userDAO.update(user);

            if (updated) {
                LOGGER.log(Level.INFO, "Password changed for user ID: {0}", userId);
            }

            return updated;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during password change", e);
            throw new ValidationException("Failed to change password due to system error");
        }
    }

    public boolean hasRole(User user, String role) {
        if (user == null || role == null) {
            return false;
        }
        return user.getRole().equalsIgnoreCase(role);
    }

    public boolean isAdmin(User user) {
        return user != null && user.isAdmin();
    }

    public boolean isManager(User user) {
        return user != null && user.isManager();
    }

    private void validateLoginInput(String username, String password)
            throws ValidationException {

        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }

        if (username.length() < 3) {
            throw new ValidationException("Username must be at least 3 characters");
        }
    }

    private void validateUserCreation(String username, String password, String role)
            throws ValidationException {

        validateLoginInput(username, password);

        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }

        if (role == null || role.trim().isEmpty()) {
            throw new ValidationException("Role cannot be empty");
        }

        String upperRole = role.toUpperCase();
        if (!upperRole.equals("ADMIN") && !upperRole.equals("STAFF") &&
            !upperRole.equals("MANAGER")) {
            throw new ValidationException("Invalid role. Must be ADMIN, STAFF, or MANAGER");
        }
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    private String hashPassword(String password) {

        return String.valueOf(password.hashCode());
    }
}
