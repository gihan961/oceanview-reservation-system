package com.oceanview.dao;

import com.oceanview.model.User;
import java.sql.SQLException;
import java.util.List;

/**
 * User Data Access Object Interface - Defines CRUD operations for User entity
 */
public interface UserDAO {
    
    /**
     * Create a new user
     * 
     * @param user User object to create
     * @return true if creation successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean create(User user) throws SQLException;
    
    /**
     * Update an existing user
     * 
     * @param user User object with updated information
     * @return true if update successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean update(User user) throws SQLException;
    
    /**
     * Delete a user by ID
     * 
     * @param id User ID
     * @return true if deletion successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean delete(int id) throws SQLException;
    
    /**
     * Find user by ID
     * 
     * @param id User ID
     * @return User object if found, null otherwise
     * @throws SQLException if database error occurs
     */
    User findById(int id) throws SQLException;
    
    /**
     * Find all users
     * 
     * @return List of all users
     * @throws SQLException if database error occurs
     */
    List<User> findAll() throws SQLException;
    
    /**
     * Find user by username
     * 
     * @param username Username
     * @return User object if found, null otherwise
     * @throws SQLException if database error occurs
     */
    User findByUsername(String username) throws SQLException;
    
    /**
     * Authenticate user
     * 
     * @param username Username
     * @param passwordHash Password hash
     * @return User object if authentication successful, null otherwise
     * @throws SQLException if database error occurs
     */
    User authenticate(String username, String passwordHash) throws SQLException;
}
