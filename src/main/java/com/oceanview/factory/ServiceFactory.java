package com.oceanview.factory;

import com.oceanview.service.AuthService;
import com.oceanview.service.ReservationService;
import com.oceanview.service.RoomService;
import com.oceanview.service.ReportService;

/**
 * Service Factory - Factory Pattern Implementation
 * Provides centralized service instance creation
 * 
 */
public class ServiceFactory {
    
    // Singleton instances of services
    private static volatile ServiceFactory instance;
    
    private AuthService authService;
    private ReservationService reservationService;
    private RoomService roomService;
    private ReportService reportService;
    
    /**
     * Private constructor to prevent direct instantiation
     */
    private ServiceFactory() {
        // Initialize services
        this.authService = new AuthService();
        this.reservationService = new ReservationService();
        this.roomService = new RoomService();
        this.reportService = new ReportService();
    }
    
    /**
     * Get singleton instance of ServiceFactory
     * Thread-safe double-checked locking
     * 
     * @return ServiceFactory instance
     */
    public static ServiceFactory getInstance() {
        if (instance == null) {
            synchronized (ServiceFactory.class) {
                if (instance == null) {
                    instance = new ServiceFactory();
                }
            }
        }
        return instance;
    }
    
    /**
     * Get AuthService instance
     * 
     * @return AuthService instance
     */
    public AuthService getAuthService() {
        if (authService == null) {
            synchronized (this) {
                if (authService == null) {
                    authService = new AuthService();
                }
            }
        }
        return authService;
    }
    
    /**
     * Get ReservationService instance
     * 
     * @return ReservationService instance
     */
    public ReservationService getReservationService() {
        if (reservationService == null) {
            synchronized (this) {
                if (reservationService == null) {
                    reservationService = new ReservationService();
                }
            }
        }
        return reservationService;
    }
    
    /**
     * Get RoomService instance
     * 
     * @return RoomService instance
     */
    public RoomService getRoomService() {
        if (roomService == null) {
            synchronized (this) {
                if (roomService == null) {
                    roomService = new RoomService();
                }
            }
        }
        return roomService;
    }
    
    /**
     * Get ReportService instance
     * 
     * @return ReportService instance
     */
    public ReportService getReportService() {
        if (reportService == null) {
            synchronized (this) {
                if (reportService == null) {
                    reportService = new ReportService();
                }
            }
        }
        return reportService;
    }
    
    /**
     * Reset factory (useful for testing)
     */
    public static void reset() {
        instance = null;
    }
}
