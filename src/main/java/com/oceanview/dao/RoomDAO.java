package com.oceanview.dao;

import com.oceanview.model.Room;
import java.sql.SQLException;
import java.util.List;

/**
 * Room Data Access Object Interface - Defines CRUD operations for Room entity
 */
public interface RoomDAO {
    
    /**
     * Create a new room
     * 
     * @param room Room object to create
     * @return true if creation successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean create(Room room) throws SQLException;
    
    /**
     * Update an existing room
     * 
     * @param room Room object with updated information
     * @return true if update successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean update(Room room) throws SQLException;
    
    /**
     * Delete a room by ID
     * 
     * @param id Room ID
     * @return true if deletion successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean delete(int id) throws SQLException;
    
    /**
     * Find room by ID
     * 
     * @param id Room ID
     * @return Room object if found, null otherwise
     * @throws SQLException if database error occurs
     */
    Room findById(int id) throws SQLException;
    
    /**
     * Find all rooms
     * 
     * @return List of all rooms
     * @throws SQLException if database error occurs
     */
    List<Room> findAll() throws SQLException;
    
    /**
     * Find rooms by status
     * 
     * @param status Room status (AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED)
     * @return List of rooms with specified status
     * @throws SQLException if database error occurs
     */
    List<Room> findByStatus(String status) throws SQLException;
    
    /**
     * Find rooms by room type
     * 
     * @param roomType Room type
     * @return List of rooms with specified type
     * @throws SQLException if database error occurs
     */
    List<Room> findByRoomType(String roomType) throws SQLException;
    
    /**
     * Update room status
     * 
     * @param roomId Room ID
     * @param status New status
     * @return true if update successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean updateStatus(int roomId, String status) throws SQLException;
}
