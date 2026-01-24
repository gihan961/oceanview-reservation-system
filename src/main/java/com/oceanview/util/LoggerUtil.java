package com.oceanview.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Centralized logging utility for the OceanView Reservation System.
 * Provides consistent logging across all application components.
 */
public class LoggerUtil {
    
    private static final String LOG_CONFIG_FILE = "/log.properties";
    private static boolean initialized = false;
    
    // Static initializer to load logging configuration
    static {
        initializeLogging();
    }
    
    /**
     * Initialize logging configuration from properties file
     */
    private static void initializeLogging() {
        if (!initialized) {
            try (InputStream configStream = LoggerUtil.class.getResourceAsStream(LOG_CONFIG_FILE)) {
                if (configStream != null) {
                    LogManager.getLogManager().readConfiguration(configStream);
                    initialized = true;
                } else {
                    System.err.println("Warning: Could not find log configuration file: " + LOG_CONFIG_FILE);
                }
            } catch (IOException e) {
                System.err.println("Error initializing logging: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get logger instance for a specific class
     * 
     * @param clazz The class requesting the logger
     * @return Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
    
    /**
     * Get logger instance by name
     * 
     * @param name The logger name
     * @return Logger instance
     */
    public static Logger getLogger(String name) {
        return Logger.getLogger(name);
    }
    
    /**
     * Log successful login attempt
     * 
     * @param username Username that logged in
     * @param ipAddress IP address of the user
     */
    public static void logLoginSuccess(String username, String ipAddress) {
        Logger logger = getLogger("com.oceanview.security.login");
        logger.info(String.format("LOGIN SUCCESS - User: %s, IP: %s", username, ipAddress));
    }
    
    /**
     * Log failed login attempt
     * 
     * @param username Username that attempted login
     * @param ipAddress IP address of the user
     * @param reason Reason for failure
     */
    public static void logLoginFailure(String username, String ipAddress, String reason) {
        Logger logger = getLogger("com.oceanview.security.login");
        logger.warning(String.format("LOGIN FAILED - User: %s, IP: %s, Reason: %s", username, ipAddress, reason));
    }
    
    /**
     * Log reservation creation
     * 
     * @param reservationId ID of the created reservation
     * @param guestName Name of the guest
     * @param roomId Room ID
     * @param username User who created the reservation
     */
    public static void logReservationCreation(int reservationId, String guestName, int roomId, String username) {
        Logger logger = getLogger("com.oceanview.business.reservation");
        logger.info(String.format("RESERVATION CREATED - ID: %d, Guest: %s, Room: %d, CreatedBy: %s", 
            reservationId, guestName, roomId, username));
    }
    
    /**
     * Log reservation update
     * 
     * @param reservationId ID of the updated reservation
     * @param username User who updated the reservation
     * @param changes Description of changes made
     */
    public static void logReservationUpdate(int reservationId, String username, String changes) {
        Logger logger = getLogger("com.oceanview.business.reservation");
        logger.info(String.format("RESERVATION UPDATED - ID: %d, UpdatedBy: %s, Changes: %s", 
            reservationId, username, changes));
    }
    
    /**
     * Log reservation deletion
     * 
     * @param reservationId ID of the deleted reservation
     * @param username User who deleted the reservation
     */
    public static void logReservationDeletion(int reservationId, String username) {
        Logger logger = getLogger("com.oceanview.business.reservation");
        logger.warning(String.format("RESERVATION DELETED - ID: %d, DeletedBy: %s", reservationId, username));
    }
    
    /**
     * Log reservation cancellation
     * 
     * @param reservationId ID of the cancelled reservation
     * @param username User who cancelled the reservation
     * @param reason Reason for cancellation
     */
    public static void logReservationCancellation(int reservationId, String username, String reason) {
        Logger logger = getLogger("com.oceanview.business.reservation");
        logger.info(String.format("RESERVATION CANCELLED - ID: %d, CancelledBy: %s, Reason: %s", 
            reservationId, username, reason));
    }
    
    /**
     * Log database error
     * 
     * @param operation The database operation being performed
     * @param error The exception that occurred
     */
    public static void logDatabaseError(String operation, Exception error) {
        Logger logger = getLogger("com.oceanview.database");
        logger.log(Level.SEVERE, String.format("DATABASE ERROR - Operation: %s, Error: %s", 
            operation, error.getMessage()), error);
    }
    
    /**
     * Log validation error
     * 
     * @param field Field that failed validation
     * @param value Value that was invalid
     * @param reason Reason for validation failure
     */
    public static void logValidationError(String field, String value, String reason) {
        Logger logger = getLogger("com.oceanview.validation");
        logger.warning(String.format("VALIDATION ERROR - Field: %s, Value: %s, Reason: %s", 
            field, value, reason));
    }
    
    /**
     * Log authentication error
     * 
     * @param username Username attempting authentication
     * @param error The exception that occurred
     */
    public static void logAuthenticationError(String username, Exception error) {
        Logger logger = getLogger("com.oceanview.security.auth");
        logger.log(Level.SEVERE, String.format("AUTHENTICATION ERROR - User: %s, Error: %s", 
            username, error.getMessage()), error);
    }
    
    /**
     * Log authorization violation
     * 
     * @param username Username attempting unauthorized action
     * @param action Action attempted
     * @param requiredRole Role required for action
     */
    public static void logAuthorizationViolation(String username, String action, String requiredRole) {
        Logger logger = getLogger("com.oceanview.security.authz");
        logger.warning(String.format("AUTHORIZATION VIOLATION - User: %s, Action: %s, RequiredRole: %s", 
            username, action, requiredRole));
    }
    
    /**
     * Log servlet request
     * 
     * @param method HTTP method
     * @param uri Request URI
     * @param username Username (if authenticated)
     * @param ipAddress IP address
     * @param statusCode Response status code
     * @param processingTime Processing time in milliseconds
     */
    public static void logRequest(String method, String uri, String username, String ipAddress, 
                                  int statusCode, long processingTime) {
        Logger logger = getLogger("com.oceanview.web.request");
        String user = (username != null) ? username : "anonymous";
        logger.info(String.format("REQUEST - %s %s | User: %s | IP: %s | Status: %d | Time: %dms", 
            method, uri, user, ipAddress, statusCode, processingTime));
    }
    
    /**
     * Log application error
     * 
     * @param component Component where error occurred
     * @param operation Operation being performed
     * @param error The exception that occurred
     */
    public static void logApplicationError(String component, String operation, Exception error) {
        Logger logger = getLogger("com.oceanview.application");
        logger.log(Level.SEVERE, String.format("APPLICATION ERROR - Component: %s, Operation: %s, Error: %s", 
            component, operation, error.getMessage()), error);
    }
    
    /**
     * Log business rule violation
     * 
     * @param rule Business rule that was violated
     * @param details Details of the violation
     */
    public static void logBusinessRuleViolation(String rule, String details) {
        Logger logger = getLogger("com.oceanview.business.rules");
        logger.warning(String.format("BUSINESS RULE VIOLATION - Rule: %s, Details: %s", rule, details));
    }
    
    /**
     * Log system event
     * 
     * @param event Event description
     * @param level Log level
     */
    public static void logSystemEvent(String event, Level level) {
        Logger logger = getLogger("com.oceanview.system");
        logger.log(level, String.format("SYSTEM EVENT - %s", event));
    }
    
    /**
     * Log performance metric
     * 
     * @param operation Operation name
     * @param duration Duration in milliseconds
     */
    public static void logPerformance(String operation, long duration) {
        Logger logger = getLogger("com.oceanview.performance");
        Level level = duration > 1000 ? Level.WARNING : Level.FINE;
        logger.log(level, String.format("PERFORMANCE - Operation: %s, Duration: %dms", operation, duration));
    }
}
