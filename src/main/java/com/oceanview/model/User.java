package com.oceanview.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * User Model Class
 * Represents a user in the OceanView Reservation System
 * 
 */
public class User {
    
    // Private fields
    private int id;
    private String username;
    private String passwordHash;
    private String role;
    private Timestamp createdAt;
    
    /**
     * Default constructor
     */
    public User() {
    }
    
    /**
     * Constructor without ID (for new users)
     * 
     * @param username user's username
     * @param passwordHash hashed password
     * @param role user's role (ADMIN, STAFF, MANAGER)
     */
    public User(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }
    
    /**
     * Full constructor with ID
     * 
     * @param id user ID
     * @param username user's username
     * @param passwordHash hashed password
     * @param role user's role
     * @param createdAt account creation timestamp
     */
    public User(int id, String username, String passwordHash, String role, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    
    /**
     * Get user ID
     * @return user ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set user ID
     * @param id user ID
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get username
     * @return username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Set username
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Get password hash
     * @return password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }
    
    /**
     * Set password hash
     * @param passwordHash password hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    /**
     * Get user role
     * @return user role
     */
    public String getRole() {
        return role;
    }
    
    /**
     * Set user role
     * @param role user role (ADMIN, STAFF, MANAGER)
     */
    public void setRole(String role) {
        this.role = role;
    }
    
    /**
     * Get creation timestamp
     * @return creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Set creation timestamp
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Check if user has admin role
     * @return true if user is admin
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    /**
     * Check if user has manager role
     * @return true if user is manager
     */
    public boolean isManager() {
        return "MANAGER".equalsIgnoreCase(role);
    }
    
    /**
     * Check if user has staff role
     * @return true if user is staff
     */
    public boolean isStaff() {
        return "STAFF".equalsIgnoreCase(role);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(username, user.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}
