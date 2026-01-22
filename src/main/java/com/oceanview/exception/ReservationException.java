package com.oceanview.exception;

/**
 * Reservation Exception
 * Thrown when reservation operations fail
 * 
 */
public class ReservationException extends BaseException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor with message
     * 
     * @param message Error message
     */
    public ReservationException(String message) {
        super(message, "RESERVATION_ERROR", 400);
    }
    
    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Throwable cause
     */
    public ReservationException(String message, Throwable cause) {
        super(message, cause, "RESERVATION_ERROR", 400);
    }
    
    /**
     * Constructor with custom error code and status
     * 
     * @param message Error message
     * @param errorCode Custom error code
     * @param httpStatus HTTP status code
     */
    public ReservationException(String message, String errorCode, int httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
