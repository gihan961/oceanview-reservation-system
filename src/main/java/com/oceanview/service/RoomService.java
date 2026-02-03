package com.oceanview.service;

import com.oceanview.dao.RoomDAO;
import com.oceanview.dao.impl.RoomDAOImpl;
import com.oceanview.exception.ValidationException;
import com.oceanview.model.Room;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Room Service
 * Handles room management business logic
 * 
 */
public class RoomService {
    
    private static final Logger LOGGER = Logger.getLogger(RoomService.class.getName());
    private final RoomDAO roomDAO;
    
    /**
     * Constructor
     */
    public RoomService() {
        this.roomDAO = new RoomDAOImpl();
    }
    
    /**
     * Constructor with custom DAO (for testing)
     * 
     * @param roomDAO Room DAO implementation
     */
    public RoomService(RoomDAO roomDAO) {
        this.roomDAO = roomDAO;
    }
    
    /**
     * Create new room
     * 
     * @param roomType Room type
     * @param pricePerNight Price per night
     * @param status Room status
     * @return Created Room object
     * @throws ValidationException if validation fails
     */
    public Room createRoom(String roomType, BigDecimal pricePerNight, String status) 
            throws ValidationException {
        
        // Validate input
        validateRoomInput(roomType, pricePerNight, status);
        
        try {
            Room room = new Room(roomType, pricePerNight, status.toUpperCase());
            boolean created = roomDAO.create(room);
            
            if (!created) {
                throw new ValidationException("Failed to create room");
            }
            
            LOGGER.log(Level.INFO, "Room created: {0}", roomType);
            return room;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error creating room", e);
            throw new ValidationException("Failed to create room due to system error");
        }
    }
    
    /**
     * Update existing room
     * 
     * @param room Room object with updated information
     * @return true if update successful
     * @throws ValidationException if validation fails
     */
    public boolean updateRoom(Room room) throws ValidationException {
        
        if (room == null) {
            throw new ValidationException("Room cannot be null");
        }
        
        validateRoomInput(room.getRoomType(), room.getPricePerNight(), room.getStatus());
        
        try {
            boolean updated = roomDAO.update(room);
            
            if (updated) {
                LOGGER.log(Level.INFO, "Room updated: ID {0}", room.getId());
            }
            
            return updated;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating room", e);
            throw new ValidationException("Failed to update room due to system error");
        }
    }
    
    /**
     * Delete room
     * 
     * @param roomId Room ID
     * @return true if deletion successful
     * @throws ValidationException if validation fails
     */
    public boolean deleteRoom(int roomId) throws ValidationException {
        
        if (roomId <= 0) {
            throw new ValidationException("Invalid room ID");
        }
        
        try {
            boolean deleted = roomDAO.delete(roomId);
            
            if (deleted) {
                LOGGER.log(Level.INFO, "Room deleted: ID {0}", roomId);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error deleting room", e);
            throw new ValidationException("Failed to delete room due to system error");
        }
    }
    
    /**
     * Get room by ID
     * 
     * @param roomId Room ID
     * @return Room object or null if not found
     * @throws ValidationException if validation fails
     */
    public Room getRoomById(int roomId) throws ValidationException {
        
        if (roomId <= 0) {
            throw new ValidationException("Invalid room ID");
        }
        
        try {
            return roomDAO.findById(roomId);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting room", e);
            throw new ValidationException("Failed to get room due to system error");
        }
    }
    
    /**
     * Get all rooms
     * 
     * @return List of all rooms
     * @throws ValidationException if operation fails
     */
    public List<Room> getAllRooms() throws ValidationException {
        
        try {
            return roomDAO.findAll();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting all rooms", e);
            throw new ValidationException("Failed to get rooms due to system error");
        }
    }
    
    /**
     * Get available rooms
     * 
     * @return List of available rooms
     * @throws ValidationException if operation fails
     */
    public List<Room> getAvailableRooms() throws ValidationException {
        
        try {
            return roomDAO.findByStatus(Room.STATUS_AVAILABLE);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting available rooms", e);
            throw new ValidationException("Failed to get available rooms due to system error");
        }
    }
    
    /**
     * Get rooms by status
     * 
     * @param status Room status
     * @return List of rooms with specified status
     * @throws ValidationException if validation fails
     */
    public List<Room> getRoomsByStatus(String status) throws ValidationException {
        
        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Status cannot be empty");
        }
        
        try {
            return roomDAO.findByStatus(status.toUpperCase());
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting rooms by status", e);
            throw new ValidationException("Failed to get rooms due to system error");
        }
    }
    
    /**
     * Get rooms by type
     * 
     * @param roomType Room type
     * @return List of rooms with specified type
     * @throws ValidationException if validation fails
     */
    public List<Room> getRoomsByType(String roomType) throws ValidationException {
        
        if (roomType == null || roomType.trim().isEmpty()) {
            throw new ValidationException("Room type cannot be empty");
        }
        
        try {
            return roomDAO.findByRoomType(roomType);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error getting rooms by type", e);
            throw new ValidationException("Failed to get rooms due to system error");
        }
    }
    
    /**
     * Update room status
     * 
     * @param roomId Room ID
     * @param status New status
     * @return true if update successful
     * @throws ValidationException if validation fails
     */
    public boolean updateRoomStatus(int roomId, String status) throws ValidationException {
        
        if (roomId <= 0) {
            throw new ValidationException("Invalid room ID");
        }
        
        validateStatus(status);
        
        try {
            boolean updated = roomDAO.updateStatus(roomId, status.toUpperCase());
            
            if (updated) {
                LOGGER.log(Level.INFO, "Room status updated: ID {0} to {1}", 
                          new Object[]{roomId, status});
            }
            
            return updated;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating room status", e);
            throw new ValidationException("Failed to update room status due to system error");
        }
    }
    
    /**
     * Check if room is available
     * 
     * @param roomId Room ID
     * @return true if room is available
     * @throws ValidationException if validation fails
     */
    public boolean isRoomAvailable(int roomId) throws ValidationException {
        
        Room room = getRoomById(roomId);
        
        if (room == null) {
            throw new ValidationException("Room not found");
        }
        
        return room.isAvailable();
    }
    
    /**
     * Validate room input
     * 
     * @param roomType Room type
     * @param pricePerNight Price per night
     * @param status Room status
     * @throws ValidationException if validation fails
     */
    private void validateRoomInput(String roomType, BigDecimal pricePerNight, String status) 
            throws ValidationException {
        
        if (roomType == null || roomType.trim().isEmpty()) {
            throw new ValidationException("Room type cannot be empty");
        }
        
        if (pricePerNight == null || pricePerNight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero");
        }
        
        validateStatus(status);
    }
    
    /**
     * Validate room status
     * 
     * @param status Room status
     * @throws ValidationException if validation fails
     */
    private void validateStatus(String status) throws ValidationException {
        
        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Status cannot be empty");
        }
        
        String upperStatus = status.toUpperCase();
        if (!upperStatus.equals(Room.STATUS_AVAILABLE) && 
            !upperStatus.equals(Room.STATUS_OCCUPIED) &&
            !upperStatus.equals(Room.STATUS_MAINTENANCE) &&
            !upperStatus.equals(Room.STATUS_RESERVED)) {
            throw new ValidationException(
                "Invalid status. Must be AVAILABLE, OCCUPIED, MAINTENANCE, or RESERVED");
        }
    }
}
