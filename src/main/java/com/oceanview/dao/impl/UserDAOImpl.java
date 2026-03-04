package com.oceanview.dao.impl;

import com.oceanview.dao.UserDAO;
import com.oceanview.model.User;
import com.oceanview.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAOImpl implements UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());
    private final DBConnection dbConnection;

    private static final String INSERT_USER =
        "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";

    private static final String UPDATE_USER =
        "UPDATE users SET username = ?, password_hash = ?, role = ? WHERE id = ?";

    private static final String DELETE_USER =
        "DELETE FROM users WHERE id = ?";

    private static final String SELECT_USER_BY_ID =
        "SELECT id, username, password_hash, role, created_at FROM users WHERE id = ?";

    private static final String SELECT_ALL_USERS =
        "SELECT id, username, password_hash, role, created_at FROM users ORDER BY id";

    private static final String SELECT_USER_BY_USERNAME =
        "SELECT id, username, password_hash, role, created_at FROM users WHERE username = ?";

    private static final String AUTHENTICATE_USER =
        "SELECT id, username, password_hash, role, created_at FROM users WHERE username = ? AND password_hash = ?";

    public UserDAOImpl() {
        this.dbConnection = DBConnection.getInstance();
    }

    @Override
    public boolean create(User user) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getRole());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
                LOGGER.log(Level.INFO, "User created successfully: {0}", user.getUsername());
                return true;
            }

            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user: " + user.getUsername(), e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public boolean update(User user) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(UPDATE_USER);

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getRole());
            pstmt.setInt(4, user.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "User updated successfully: {0}", user.getUsername());
                return true;
            }

            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user: " + user.getUsername(), e);
            throw e;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(DELETE_USER);

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "User deleted successfully: ID {0}", id);
                return true;
            }

            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user: ID " + id, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    @Override
    public User findById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_USER_BY_ID);

            pstmt.setInt(1, id);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

            return null;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by ID: " + id, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_ALL_USERS);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

            LOGGER.log(Level.INFO, "Found {0} users", users.size());
            return users;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all users", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public User findByUsername(String username) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_USER_BY_USERNAME);

            pstmt.setString(1, username);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

            return null;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by username: " + username, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public User authenticate(String username, String passwordHash) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(AUTHENTICATE_USER);

            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                LOGGER.log(Level.INFO, "User authenticated successfully: {0}", username);
                return extractUserFromResultSet(rs);
            }

            LOGGER.log(Level.WARNING, "Authentication failed for user: {0}", username);
            return null;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error authenticating user: " + username, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("role"),
            rs.getTimestamp("created_at")
        );
    }

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database resources", e);
        }
    }
}
