package com.oceanview.exception;

public class ReservationException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ReservationException(String message) {
        super(message, "RESERVATION_ERROR", 400);
    }

    public ReservationException(String message, Throwable cause) {
        super(message, cause, "RESERVATION_ERROR", 400);
    }

    public ReservationException(String message, String errorCode, int httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
