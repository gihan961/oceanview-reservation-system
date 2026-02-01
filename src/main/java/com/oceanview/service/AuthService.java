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

/**
 * Authentication Service
 * Handles user authentication and authorization
 * 
 */
public class AuthService {
    
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private final UserDAO userDAO;
    
    /**
     * Constructor
     */
    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }
    
    /**
     * Constructor with custom DAO (for testing)
     * 
     * @param userDAO User DAO implementation
     */
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    /**
     * Authenticate user with username and password
     * 
     * @param username Username
     * @param password Plain text password
     * @return User object if authentication successful
     * @throws AuthenticationException if authentication fails
     * @throws ValidationException if input validation fails
     */
    public User login(String username, String password) 
            throws AuthenticationException, ValidationException {
        
        // Validate input
        validateLoginInput(username, password);
        
        try {
            // Hash the password (in production, use bcrypt or similar)
            String passwordHash = hashPassword(password);
            
            // Authenticate user
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
    
    /**
     * Create new user account
     * 
     * @param username Username
     * @param password Plain text password
     * @param role User role (ADMIN, STAFF, MANAGER)
     * @return Created User object
     * @throws ValidationException if validation fails
     */
    public User createUser(String username, String password, String role) 
            throws ValidationException {
        
        // Validate input
        validateUserCreation(username, password, role);
        
        try {
            // Check if username already exists
            User existingUser = userDAO.findByUsername(username);
            if (existingUser != null) {
                throw new ValidationException("Username already exists");
            }
            
            // Hash password
            String passwordHash = hashPassword(password);
            
            // Create user
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
    
    /**
     * Register new user (for public registration)
     * This method is used for user self-registration and has additional restrictions:
     * - Cannot register as ADMIN
     * - Only MANAGER and STAFF roles are allowed
     * 
     * @param username Username
     * @param password Plain text password
     * @param role User role (MANAGER or STAFF only)
     * @param fullName Full name of the user
     * @return Created User object
     * @throws ValidationException if validation fails
     * @throws IllegalStateException if username already exists
     */
    public User registerUser(String username, String password, String role, String fullName) 
            throws ValidationException, IllegalStateException {
        
        // Validate basic input
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
        
        // Validate username length
        if (username.trim().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long");
        }
        
        // Validate password length
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }
        
        // Validate full name length
        if (fullName.trim().length() < 2) {
            throw new ValidationException("Full name must be at least 2 characters long");
        }
        
        // CRITICAL: Prevent registration as ADMIN
        String normalizedRole = role.trim().toUpperCase();
        if (normalizedRole.equals("ADMIN")) {
            throw new ValidationException("Cannot register as Admin. Only Manager and Staff roles are allowed.");
        }
        
        // Validate role is MANAGER or STAFF
        if (!normalizedRole.equals("MANAGER") && !normalizedRole.equals("STAFF")) {
            throw new ValidationException("Invalid role. Must be MANAGER or STAFF");
        }
        
        try {
            // Check if username already exists
            User existingUser = userDAO.findByUsername(username.trim());
            if (existingUser != null) {
                LOGGER.log(Level.WARNING, "Registration failed - username already exists: {0}", username);
                throw new IllegalStateException("Username '" + username + "' is already taken. Please choose a different username.");
            }
            
            // Hash password
            String passwordHash = hashPassword(password);
            
            // Create user (Note: User model might need to be updated to include fullName)
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
    
    /**
     * Change user password
     * 
     * @param userId User ID
     * @param oldPassword Old password
     * @param newPassword New password
     * @return true if password changed successfully
     * @throws AuthenticationException if old password is incorrect
     * @throws ValidationException if validation fails
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) 
            throws AuthenticationException, ValidationException {
        
        // Validate new password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new ValidationException("New password cannot be empty");
        }
        
        if (newPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }
        
        try {
            // Get user
            User user = userDAO.findById(userId);
            if (user == null) {
                throw new ValidationException("User not found");
            }
            
            // Verify old password
            String oldPasswordHash = hashPassword(oldPassword);
            if (!user.getPasswordHash().equals(oldPasswordHash)) {
                throw new AuthenticationException("Old password is incorrect");
            }
            
            // Update password
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
    
    /**
     * Check if user has specific role
     * 
     * @param user User object
     * @param role Required role
     * @return true if user has the role
     */
    public boolean hasRole(User user, String role) {
        if (user == null || role == null) {
            return false;
        }
        return user.getRole().equalsIgnoreCase(role);
    }
    
    /**
     * Check if user is admin
     * 
     * @param user User object
     * @return true if user is admin
     */
    public boolean isAdmin(User user) {
        return user != null && user.isAdmin();
    }
    
    /**
     * Check if user is manager
     * 
     * @param user User object
     * @return true if user is manager
     */
    public boolean isManager(User user) {
        return user != null && user.isManager();
    }
    
    /**
     * Validate login input
     * 
     * @param username Username
     * @param password Password
     * @throws ValidationException if validation fails
     */
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
    
    /**
     * Validate user creation input
     * 
     * @param username Username
     * @param password Password
     * @param role User role
     * @throws ValidationException if validation fails
     */
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
    
    /**
     * Get all users (Admin only — caller must enforce RBAC)
     *
     * @return list of all users
     * @throws SQLException if database error occurs
     */
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    /**
     * Hash password (simple implementation - use bcrypt in production)
     * 
     * @param password Plain text password
     * @return Hashed password
     */
    private String hashPassword(String password) {
        // In production, use BCrypt or similar:
        // return BCrypt.hashpw(password, BCrypt.gensalt());
        
        // Simple hash for demonstration (NOT SECURE - use proper hashing in production)
        return String.valueOf(password.hashCode());
    }
}
