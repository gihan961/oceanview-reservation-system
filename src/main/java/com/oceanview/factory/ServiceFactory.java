package com.oceanview.factory;

import com.oceanview.service.AuthService;
import com.oceanview.service.ReservationService;
import com.oceanview.service.RoomService;
import com.oceanview.service.ReportService;

public class ServiceFactory {

    private static volatile ServiceFactory instance;

    private AuthService authService;
    private ReservationService reservationService;
    private RoomService roomService;
    private ReportService reportService;

    private ServiceFactory() {

        this.authService = new AuthService();
        this.reservationService = new ReservationService();
        this.roomService = new RoomService();
        this.reportService = new ReportService();
    }

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

    public static void reset() {
        instance = null;
    }
}
