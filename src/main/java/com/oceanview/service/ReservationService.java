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

/**
 * Reservation Service
 * Handles reservation management business logic
 * 
 */
public class ReservationService {
    
    private static final Logger LOGGER = Logger.getLogger(ReservationService.class.getName());
    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMdd");
    private static int reservationCounter = 1;
    
    /**
     * Constructor
     */
    public ReservationService() {
        this.reservationDAO = new ReservationDAOImpl();
        this.roomDAO = new RoomDAOImpl();
    }
    
    /**
     * Constructor with custom DAOs (for testing)
     * 
     * @param reservationDAO Reservation DAO
     * @param roomDAO Room DAO
     */
    public ReservationService(ReservationDAO reservationDAO, RoomDAO roomDAO) {
        this.reservationDAO = reservationDAO;
        this.roomDAO = roomDAO;
    }
    
    /**
     * Create new reservation
     * 
     * @param guestName Guest name
     * @param address Guest address
     * @param contactNumber Guest contact number
     * @param roomId Room ID
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @return Created Reservation object
     * @throws ReservationException if reservation fails
     * @throws ValidationException if validation fails
     */
    public Reservation createReservation(
            String guestName,
            String address,
            String contactNumber,
            int roomId,
            Date checkInDate,
            Date checkOutDate) 
            throws ReservationException, ValidationException {
        
        // Validate input
        validateReservationInput(guestName, address, contactNumber, roomId, 
                                checkInDate, checkOutDate);
        
        // Validate dates
        validateDates(checkInDate, checkOutDate);
        
        // Check room availability
        if (!checkRoomAvailability(roomId, checkInDate, checkOutDate)) {
            throw new ReservationException(
                "Room is not available for the selected dates");
        }
        
        try {
            // Get room details
            Room room = roomDAO.findById(roomId);
            if (room == null) {
                throw new ValidationException("Room not found");
            }
            
            // Calculate total bill
            BigDecimal totalAmount = calculateTotalBill(
                room.getPricePerNight(), 
                checkInDate, 
                checkOutDate
            );
            
            // Generate reservation number
            String reservationNumber = generateReservationNumber();
            
            // Create reservation
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
            
            // Update room status to RESERVED
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
    
    /**
     * Update existing reservation
     * 
     * @param reservation Reservation object with updated information
     * @return true if update successful
     * @throws ReservationException if update fails
     * @throws ValidationException if validation fails
     */
    public boolean updateReservation(Reservation reservation) 
            throws ReservationException, ValidationException {
        
        if (reservation == null) {
            throw new ValidationException("Reservation cannot be null");
        }
        
        // Validate input
        validateReservationInput(
            reservation.getGuestName(),
            reservation.getAddress(),
            reservation.getContactNumber(),
            reservation.getRoomId(),
            reservation.getCheckInDate(),
            reservation.getCheckOutDate()
        );
        
        // Validate dates
        validateDates(reservation.getCheckInDate(), reservation.getCheckOutDate());
        
        try {
            // Get room details
            Room room = roomDAO.findById(reservation.getRoomId());
            if (room == null) {
                throw new ValidationException("Room not found");
            }
            
            // Recalculate total bill
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
    
    /**
     * Cancel reservation
     * 
     * @param reservationId Reservation ID
     * @return true if cancellation successful
     * @throws ReservationException if cancellation fails
     * @throws ValidationException if validation fails
     */
    public boolean cancelReservation(int reservationId) 
            throws ReservationException, ValidationException {
        
        if (reservationId <= 0) {
            throw new ValidationException("Invalid reservation ID");
        }
        
        try {
            // Get reservation to find room ID
            Reservation reservation = reservationDAO.findById(reservationId);
            
            if (reservation == null) {
                throw new ValidationException("Reservation not found");
            }
            
            // Delete reservation
            boolean deleted = reservationDAO.delete(reservationId);
            
            if (deleted) {
                // Update room status back to AVAILABLE
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
    
    /**
     * Get reservation by ID
     * 
     * @param reservationId Reservation ID
     * @return Reservation object or null if not found
     * @throws ValidationException if validation fails
     */
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
    
    /**
     * Get reservation by reservation number
     * 
     * @param reservationNumber Reservation number
     * @return Reservation object or null if not found
     * @throws ValidationException if validation fails
     */
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
    
    /**
     * Get all reservations
     * 
     * @return List of all reservations
     * @throws ValidationException if operation fails
     */
    public List<Reservation> getAllReservations() throws ValidationException {
        
        try {
            return reservationDAO.findAll();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting all reservations", e);
            throw new ValidationException("Failed to get reservations: " + e.getMessage());
        }
    }
    
    /**
     * Search reservations by guest name
     * 
     * @param guestName Guest name (partial match supported)
     * @return List of matching reservations
     * @throws ValidationException if validation fails
     */
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
    
    /**
     * Get today's check-ins
     * 
     * @return List of reservations checking in today
     * @throws ValidationException if operation fails
     */
    public List<Reservation> getTodayCheckIns() throws ValidationException {
        
        try {
            return reservationDAO.findCheckInsToday();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting today's check-ins", e);
            throw new ValidationException("Failed to get check-ins: " + e.getMessage());
        }
    }
    
    /**
     * Get today's check-outs
     * 
     * @return List of reservations checking out today
     * @throws ValidationException if operation fails
     */
    public List<Reservation> getTodayCheckOuts() throws ValidationException {
        
        try {
            return reservationDAO.findCheckOutsToday();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting today's check-outs", e);
            throw new ValidationException("Failed to get check-outs: " + e.getMessage());
        }
    }
    
    /**
     * Check room availability for dates
     * 
     * @param roomId Room ID
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @return true if room is available
     */
    public boolean checkRoomAvailability(int roomId, Date checkInDate, Date checkOutDate) {
        
        try {
            // Get room
            Room room = roomDAO.findById(roomId);
            
            if (room == null || room.isMaintenance()) {
                return false;
            }
            
            // Get existing reservations for this room
            List<Reservation> existingReservations = reservationDAO.findByRoomId(roomId);
            
            // Check for date overlaps
            LocalDate newCheckIn = checkInDate.toLocalDate();
            LocalDate newCheckOut = checkOutDate.toLocalDate();
            
            for (Reservation reservation : existingReservations) {
                LocalDate existingCheckIn = reservation.getCheckInDate().toLocalDate();
                LocalDate existingCheckOut = reservation.getCheckOutDate().toLocalDate();
                
                // Check if dates overlap
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
    
    /**
     * Calculate total bill
     * 
     * @param pricePerNight Price per night
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @return Total amount
     * @throws ValidationException if calculation fails
     */
    public BigDecimal calculateTotalBill(BigDecimal pricePerNight, Date checkInDate, 
                                        Date checkOutDate) throws ValidationException {
        
        if (pricePerNight == null || pricePerNight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Invalid price per night");
        }
        
        validateDates(checkInDate, checkOutDate);
        
        // Calculate number of nights
        long nights = ChronoUnit.DAYS.between(
            checkInDate.toLocalDate(),
            checkOutDate.toLocalDate()
        );
        
        // Calculate total
        BigDecimal total = pricePerNight.multiply(BigDecimal.valueOf(nights));
        
        LOGGER.log(Level.INFO, "Bill calculated: {0} nights x {1} = {2}", 
                  new Object[]{nights, pricePerNight, total});
        
        return total;
    }
    
    /**
     * Generate unique reservation number
     * Format: RES + YYYYMMDD + sequential number (3 digits)
     * 
     * @return Generated reservation number
     */
    public synchronized String generateReservationNumber() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String reservationNumber;
        int attempts = 0;
        int maxAttempts = 1000; // Maximum attempts to find unique number
        
        do {
            String counterPart = String.format("%03d", reservationCounter);
            reservationNumber = "RES" + datePart + counterPart;
            reservationCounter++;
            
            // Reset counter at midnight (simple implementation)
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
    
    /**
     * Check if reservation number already exists in database
     * 
     * @param reservationNumber Reservation number to check
     * @return true if exists, false otherwise
     */
    private boolean isReservationNumberExists(String reservationNumber) {
        try {
            Reservation existing = reservationDAO.findByReservationNumber(reservationNumber);
            return existing != null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking reservation number existence: " + e.getMessage(), e);
            return false; // If error, assume doesn't exist and let database constraint handle it
        }
    }
    
    /**
     * Validate reservation input
     * 
     * @param guestName Guest name
     * @param address Guest address
     * @param contactNumber Contact number
     * @param roomId Room ID
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @throws ValidationException if validation fails
     */
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
    
    /**
     * Validate dates
     * 
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @throws ValidationException if validation fails
     */
    private void validateDates(Date checkInDate, Date checkOutDate) 
            throws ValidationException {
        
        LocalDate checkIn = checkInDate.toLocalDate();
        LocalDate checkOut = checkOutDate.toLocalDate();
        LocalDate today = LocalDate.now();
        
        // Check if check-in is in the past
        if (checkIn.isBefore(today)) {
            throw new ValidationException("Check-in date cannot be in the past");
        }
        
        // Check if check-out is after check-in
        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException("Check-out date must be after check-in date");
        }
        
        // Check if reservation is too long (e.g., max 30 days)
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights > 30) {
            throw new ValidationException("Reservation cannot exceed 30 nights");
        }
        
        // Check if reservation is too far in advance (e.g., max 365 days)
        long daysInAdvance = ChronoUnit.DAYS.between(today, checkIn);
        if (daysInAdvance > 365) {
            throw new ValidationException("Reservation cannot be made more than 365 days in advance");
        }
    }
    
    /**
     * Check if two date ranges overlap
     * 
     * @param start1 Start date of first range
     * @param end1 End date of first range
     * @param start2 Start date of second range
     * @param end2 End date of second range
     * @return true if dates overlap
     */
    private boolean datesOverlap(LocalDate start1, LocalDate end1, 
                                LocalDate start2, LocalDate end2) {
        return !(end1.isBefore(start2) || end1.equals(start2) || 
                start1.isAfter(end2) || start1.equals(end2));
    }
}
