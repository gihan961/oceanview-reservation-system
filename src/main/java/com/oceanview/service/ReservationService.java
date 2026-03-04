package com.oceanview.service;

import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.RoomDAO;
import com.oceanview.dao.impl.ReservationDAOImpl;
import com.oceanview.dao.impl.RoomDAOImpl;
import com.oceanview.exception.ReservationException;
import com.oceanview.exception.ValidationException;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationService {

    private static final Logger LOGGER = Logger.getLogger(ReservationService.class.getName());
    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd");
    private static int reservationCounter = 1;

    public ReservationService() {
        this.reservationDAO = new ReservationDAOImpl();
        this.roomDAO = new RoomDAOImpl();
    }

    public ReservationService(ReservationDAO reservationDAO, RoomDAO roomDAO) {
        this.reservationDAO = reservationDAO;
        this.roomDAO = roomDAO;
    }

    public Reservation createReservation(
            String guestName,
            String address,
            String contactNumber,
            int roomId,
            Date checkInDate,
            Date checkOutDate)
            throws ReservationException, ValidationException {

        validateReservationInput(guestName, address, contactNumber, roomId,
                                checkInDate, checkOutDate);

        validateDates(checkInDate, checkOutDate);

        if (!checkRoomAvailability(roomId, checkInDate, checkOutDate)) {
            throw new ReservationException(
                "Room is not available for the selected dates");
        }

        try {

            Room room = roomDAO.findById(roomId);
            if (room == null) {
                throw new ValidationException("Room not found");
            }

            BigDecimal totalAmount = calculateTotalBill(
                room.getPricePerNight(),
                checkInDate,
                checkOutDate
            );

            String reservationNumber = generateReservationNumber();

            Reservation reservation = new Reservation(
                reservationNumber,
                guestName,
                address,
                contactNumber,
                roomId,
                checkInDate,
                checkOutDate,
                totalAmount
            );

            boolean created = reservationDAO.create(reservation);

            if (!created) {
                throw new ReservationException("Failed to create reservation");
            }

            roomDAO.updateStatus(roomId, Room.STATUS_RESERVED);

            LOGGER.log(Level.INFO, "Reservation created: {0}", reservationNumber);
            return reservation;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating reservation", e);
            if (e instanceof ReservationException) {
                throw (ReservationException) e;
            } else if (e instanceof ValidationException) {
                throw (ValidationException) e;
            } else {
                throw new ReservationException("Failed to create reservation: " + e.getMessage());
            }
        }
    }

    public boolean updateReservation(Reservation reservation)
            throws ReservationException, ValidationException {

        if (reservation == null) {
            throw new ValidationException("Reservation cannot be null");
        }

        validateReservationInput(
            reservation.getGuestName(),
            reservation.getAddress(),
            reservation.getContactNumber(),
            reservation.getRoomId(),
            reservation.getCheckInDate(),
            reservation.getCheckOutDate()
        );

        validateDates(reservation.getCheckInDate(), reservation.getCheckOutDate());

        try {

            Room room = roomDAO.findById(reservation.getRoomId());
            if (room == null) {
                throw new ValidationException("Room not found");
            }

            BigDecimal totalAmount = calculateTotalBill(
                room.getPricePerNight(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
            );
            reservation.setTotalAmount(totalAmount);

            boolean updated = reservationDAO.update(reservation);

            if (updated) {
                LOGGER.log(Level.INFO, "Reservation updated: {0}",
                          reservation.getReservationNumber());
            }

            return updated;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating reservation", e);
            if (e instanceof ValidationException) {
                throw (ValidationException) e;
            } else {
                throw new ReservationException("Failed to update reservation: " + e.getMessage());
            }
        }
    }

    public boolean cancelReservation(int reservationId)
            throws ReservationException, ValidationException {

        if (reservationId <= 0) {
            throw new ValidationException("Invalid reservation ID");
        }

        try {

            Reservation reservation = reservationDAO.findById(reservationId);

            if (reservation == null) {
                throw new ValidationException("Reservation not found");
            }

            boolean deleted = reservationDAO.delete(reservationId);

            if (deleted) {

                roomDAO.updateStatus(reservation.getRoomId(), Room.STATUS_AVAILABLE);

                LOGGER.log(Level.INFO, "Reservation cancelled: {0}",
                          reservation.getReservationNumber());
            }

            return deleted;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cancelling reservation", e);
            if (e instanceof ValidationException) {
                throw (ValidationException) e;
            } else {
                throw new ReservationException("Failed to cancel reservation: " + e.getMessage());
            }
        }
    }

    public Reservation getReservationById(int reservationId) throws ValidationException {

        if (reservationId <= 0) {
            throw new ValidationException("Invalid reservation ID");
        }

        try {
            return reservationDAO.findById(reservationId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting reservation", e);
            throw new ValidationException("Failed to get reservation: " + e.getMessage());
        }
    }

    public Reservation getReservationByNumber(String reservationNumber)
            throws ValidationException {

        if (reservationNumber == null || reservationNumber.trim().isEmpty()) {
            throw new ValidationException("Reservation number cannot be empty");
        }

        try {
            return reservationDAO.findByReservationNumber(reservationNumber);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting reservation by number", e);
            throw new ValidationException("Failed to get reservation: " + e.getMessage());
        }
    }

    public List<Reservation> getAllReservations() throws ValidationException {

        try {
            return reservationDAO.findAll();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting all reservations", e);
            throw new ValidationException("Failed to get reservations: " + e.getMessage());
        }
    }

    public List<Reservation> searchReservationsByGuest(String guestName)
            throws ValidationException {

        if (guestName == null || guestName.trim().isEmpty()) {
            throw new ValidationException("Guest name cannot be empty");
        }

        try {
            return reservationDAO.findByGuestName(guestName);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error searching reservations", e);
            throw new ValidationException("Failed to search reservations: " + e.getMessage());
        }
    }

    public List<Reservation> getTodayCheckIns() throws ValidationException {

        try {
            return reservationDAO.findCheckInsToday();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting today's check-ins", e);
            throw new ValidationException("Failed to get check-ins: " + e.getMessage());
        }
    }

    public List<Reservation> getTodayCheckOuts() throws ValidationException {

        try {
            return reservationDAO.findCheckOutsToday();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting today's check-outs", e);
            throw new ValidationException("Failed to get check-outs: " + e.getMessage());
        }
    }

    public boolean checkRoomAvailability(int roomId, Date checkInDate, Date checkOutDate) {

        try {

            Room room = roomDAO.findById(roomId);

            if (room == null || room.isMaintenance()) {
                return false;
            }

            List<Reservation> existingReservations = reservationDAO.findByRoomId(roomId);

            LocalDate newCheckIn = checkInDate.toLocalDate();
            LocalDate newCheckOut = checkOutDate.toLocalDate();

            for (Reservation reservation : existingReservations) {
                LocalDate existingCheckIn = reservation.getCheckInDate().toLocalDate();
                LocalDate existingCheckOut = reservation.getCheckOutDate().toLocalDate();

                if (datesOverlap(newCheckIn, newCheckOut, existingCheckIn, existingCheckOut)) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking room availability", e);
            return false;
        }
    }

    public BigDecimal calculateTotalBill(BigDecimal pricePerNight, Date checkInDate,
                                        Date checkOutDate) throws ValidationException {

        if (pricePerNight == null || pricePerNight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Invalid price per night");
        }

        validateDates(checkInDate, checkOutDate);

        long nights = ChronoUnit.DAYS.between(
            checkInDate.toLocalDate(),
            checkOutDate.toLocalDate()
        );

        BigDecimal total = pricePerNight.multiply(BigDecimal.valueOf(nights));

        LOGGER.log(Level.INFO, "Bill calculated: {0} nights x {1} = {2}",
                  new Object[]{nights, pricePerNight, total});

        return total;
    }

    public synchronized String generateReservationNumber() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String reservationNumber;
        int attempts = 0;
        int maxAttempts = 1000;

        do {
            String counterPart = String.format("%03d", reservationCounter);
            reservationNumber = "RES" + datePart + counterPart;
            reservationCounter++;

            if (reservationCounter > 999) {
                reservationCounter = 1;
            }

            attempts++;
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Unable to generate unique reservation number after " + maxAttempts + " attempts");
            }

        } while (isReservationNumberExists(reservationNumber));

        return reservationNumber;
    }

    private boolean isReservationNumberExists(String reservationNumber) {
        try {
            Reservation existing = reservationDAO.findByReservationNumber(reservationNumber);
            return existing != null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking reservation number existence: " + e.getMessage(), e);
            return false;
        }
    }

    private void validateReservationInput(String guestName, String address,
                                         String contactNumber, int roomId,
                                         Date checkInDate, Date checkOutDate)
            throws ValidationException {

        if (guestName == null || guestName.trim().isEmpty()) {
            throw new ValidationException("Guest name cannot be empty");
        }

        if (guestName.length() < 2) {
            throw new ValidationException("Guest name must be at least 2 characters");
        }

        if (address == null || address.trim().isEmpty()) {
            throw new ValidationException("Address cannot be empty");
        }

        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            throw new ValidationException("Contact number cannot be empty");
        }

        if (contactNumber.length() < 7) {
            throw new ValidationException("Contact number must be at least 7 digits");
        }

        if (roomId <= 0) {
            throw new ValidationException("Invalid room ID");
        }

        if (checkInDate == null) {
            throw new ValidationException("Check-in date cannot be null");
        }

        if (checkOutDate == null) {
            throw new ValidationException("Check-out date cannot be null");
        }
    }

    private void validateDates(Date checkInDate, Date checkOutDate)
            throws ValidationException {

        LocalDate checkIn = checkInDate.toLocalDate();
        LocalDate checkOut = checkOutDate.toLocalDate();
        LocalDate today = LocalDate.now();

        if (checkIn.isBefore(today)) {
            throw new ValidationException("Check-in date cannot be in the past");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException("Check-out date must be after check-in date");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights > 30) {
            throw new ValidationException("Reservation cannot exceed 30 nights");
        }

        long daysInAdvance = ChronoUnit.DAYS.between(today, checkIn);
        if (daysInAdvance > 365) {
            throw new ValidationException("Reservation cannot be made more than 365 days in advance");
        }
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1,
                                LocalDate start2, LocalDate end2) {
        return !(end1.isBefore(start2) || end1.equals(start2) ||
                start1.isAfter(end2) || start1.equals(end2));
    }
}
