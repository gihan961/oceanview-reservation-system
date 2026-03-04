package com.oceanview.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggerUtil {

    private static final String LOG_CONFIG_FILE = "/log.properties";
    private static boolean initialized = false;

    static {
        initializeLogging();
    }

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

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        return Logger.getLogger(name);
    }

    public static void logLoginSuccess(String username, String ipAddress) {
        Logger logger = getLogger("com.oceanview.security.login");
        logger.info(String.format("LOGIN SUCCESS - User: %s, IP: %s", username, ipAddress));
    }

    public static void logLoginFailure(String username, String ipAddress, String reason) {
        Logger logger = getLogger("com.oceanview.security.login");
        logger.warning(String.format("LOGIN FAILED - User: %s, IP: %s, Reason: %s", username, ipAddress, reason));
    }

    public static void logReservationCreation(int reservationId, String guestName, int roomId, String username) {
        Logger logger = getLogger("com.oceanview.business.reservation");
        logger.info(String.format("RESERVATION CREATED - ID: %d, Guest: %s, Room: %d, CreatedBy: %s",
            reservationId, guestName, roomId, username));
    }

    public static void logReservationUpdate(int reservationId, String username, String changes) {
        Logger logger = getLogger("com.oceanview.business.reservation");
        logger.info(String.format("RESERVATION UPDATED - ID: %d, UpdatedBy: %s, Changes: %s",
            reservationId, username, changes));
    }

    public static void logReservationDeletion(int reservationId, String username) {
        Logger logger = getLogger("com.oceanview.business.reservation");
        logger.warning(String.format("RESERVATION DELETED - ID: %d, DeletedBy: %s", reservationId, username));
    }

    public static void logReservationCancellation(int reservationId, String username, String reason) {
        Logger logger = getLogger("com.oceanview.business.reservation");
        logger.info(String.format("RESERVATION CANCELLED - ID: %d, CancelledBy: %s, Reason: %s",
            reservationId, username, reason));
    }

    public static void logDatabaseError(String operation, Exception error) {
        Logger logger = getLogger("com.oceanview.database");
        logger.log(Level.SEVERE, String.format("DATABASE ERROR - Operation: %s, Error: %s",
            operation, error.getMessage()), error);
    }

    public static void logValidationError(String field, String value, String reason) {
        Logger logger = getLogger("com.oceanview.validation");
        logger.warning(String.format("VALIDATION ERROR - Field: %s, Value: %s, Reason: %s",
            field, value, reason));
    }

    public static void logAuthenticationError(String username, Exception error) {
        Logger logger = getLogger("com.oceanview.security.auth");
        logger.log(Level.SEVERE, String.format("AUTHENTICATION ERROR - User: %s, Error: %s",
            username, error.getMessage()), error);
    }

    public static void logAuthorizationViolation(String username, String action, String requiredRole) {
        Logger logger = getLogger("com.oceanview.security.authz");
        logger.warning(String.format("AUTHORIZATION VIOLATION - User: %s, Action: %s, RequiredRole: %s",
            username, action, requiredRole));
    }

    public static void logRequest(String method, String uri, String username, String ipAddress,
                                  int statusCode, long processingTime) {
        Logger logger = getLogger("com.oceanview.web.request");
        String user = (username != null) ? username : "anonymous";
        logger.info(String.format("REQUEST - %s %s | User: %s | IP: %s | Status: %d | Time: %dms",
            method, uri, user, ipAddress, statusCode, processingTime));
    }

    public static void logApplicationError(String component, String operation, Exception error) {
        Logger logger = getLogger("com.oceanview.application");
        logger.log(Level.SEVERE, String.format("APPLICATION ERROR - Component: %s, Operation: %s, Error: %s",
            component, operation, error.getMessage()), error);
    }

    public static void logBusinessRuleViolation(String rule, String details) {
        Logger logger = getLogger("com.oceanview.business.rules");
        logger.warning(String.format("BUSINESS RULE VIOLATION - Rule: %s, Details: %s", rule, details));
    }

    public static void logSystemEvent(String event, Level level) {
        Logger logger = getLogger("com.oceanview.system");
        logger.log(level, String.format("SYSTEM EVENT - %s", event));
    }

    public static void logPerformance(String operation, long duration) {
        Logger logger = getLogger("com.oceanview.performance");
        Level level = duration > 1000 ? Level.WARNING : Level.FINE;
        logger.log(level, String.format("PERFORMANCE - Operation: %s, Duration: %dms", operation, duration));
    }
}
