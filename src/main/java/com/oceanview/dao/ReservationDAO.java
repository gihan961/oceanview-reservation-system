package com.oceanview.dao;

import com.oceanview.model.Reservation;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * Reservation Data Access Object Interface
 * Defines CRUD operations for Reservation entity
 * 
 */
public interface ReservationDAO {
    
    /**
     * Create a new reservation
     * 
     * @param reservation Reservation object to create
     * @return true if creation successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean create(Reservation reservation) throws SQLException;
    
    /**
     * Update an existing reservation
     * 
     * @param reservation Reservation object with updated information
     * @return true if update successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean update(Reservation reservation) throws SQLException;
    
    /**
     * Delete a reservation by ID
     * 
     * @param id Reservation ID
     * @return true if deletion successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean delete(int id) throws SQLException;
    
    /**
     * Find reservation by ID
     * 
     * @param id Reservation ID
     * @return Reservation object if found, null otherwise
     * @throws SQLException if database error occurs
     */
    Reservation findById(int id) throws SQLException;
    
    /**
     * Find all reservations
     * 
     * @return List of all reservations
     * @throws SQLException if database error occurs
     */
    List<Reservation> findAll() throws SQLException;
    
    /**
     * Find reservation by reservation number
     * 
     * @param reservationNumber Reservation number
     * @return Reservation object if found, null otherwise
     * @throws SQLException if database error occurs
     */
    Reservation findByReservationNumber(String reservationNumber) throws SQLException;
    
    /**
     * Find reservations by guest name
     * 
     * @param guestName Guest name
     * @return List of reservations for the guest
     * @throws SQLException if database error occurs
     */
    List<Reservation> findByGuestName(String guestName) throws SQLException;
    
    /**
     * Find reservations by room ID
     * 
     * @param roomId Room ID
     * @return List of reservations for the room
     * @throws SQLException if database error occurs
     */
    List<Reservation> findByRoomId(int roomId) throws SQLException;
    
    /**
     * Find reservations by date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return List of reservations within date range
     * @throws SQLException if database error occurs
     */
    List<Reservation> findByDateRange(Date startDate, Date endDate) throws SQLException;
    
    /**
     * Find reservations checking in today
     * 
     * @return List of reservations checking in today
     * @throws SQLException if database error occurs
     */
    List<Reservation> findCheckInsToday() throws SQLException;
    
    /**
     * Find reservations checking out today
     * 
     * @return List of reservations checking out today
     * @throws SQLException if database error occurs
     */
    List<Reservation> findCheckOutsToday() throws SQLException;
}
