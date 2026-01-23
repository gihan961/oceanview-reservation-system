package com.oceanview.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Connection Utility Class using Singleton Pattern
 * Provides reusable database connections for the OceanView Reservation System
 * 
 */
public class DBConnection {
    
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    
    // Singleton instance
    private static DBConnection instance;
    
    // Database configuration properties
    private Properties properties;
    private String driver;
    private String url;
    private String username;
    private String password;
    
    // Configuration file path
    private static final String PROPERTIES_FILE = "db.properties";
    
    /**
     * Private constructor to prevent instantiation
     * Loads database configuration from db.properties file
     */
    private DBConnection() {
        loadProperties();
    }
    
    /**
     * Get the singleton instance of DBConnection
     * Thread-safe implementation using double-checked locking
     * 
     * @return DBConnection instance
     */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }
    
    /**
     * Load database properties from db.properties file
     * Initializes database driver
     */
    private void loadProperties() {
        properties = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IOException("Unable to find " + PROPERTIES_FILE);
            }
            
            // Load properties from file
            properties.load(input);
            
            // Extract database configuration
            driver = properties.getProperty("db.driver");
            url = properties.getProperty("db.url");
            username = properties.getProperty("db.username");
            password = properties.getProperty("db.password");
            
            // Load JDBC driver
            Class.forName(driver);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading database properties", e);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "JDBC Driver not found", e);
        }
    }
    
    /**
     * Get a database connection
     * Creates a new connection to the database using loaded properties
     * 
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection", e);
            throw e;
        }
    }
    
    /**
     * Close database connection safely
     * 
     * @param connection Connection to close
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
    
    /**
     * Test database connection
     * 
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        Connection connection = null;
        try {
            connection = getConnection();
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Connection test failed", e);
            return false;
        } finally {
            closeConnection(connection);
        }
    }
    
    /**
     * Get database URL
     * 
     * @return database URL
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Get database username
     * 
     * @return database username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Get property value by key
     * 
     * @param key property key
     * @return property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
