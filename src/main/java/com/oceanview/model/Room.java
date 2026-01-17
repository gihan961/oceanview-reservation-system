package com.oceanview.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Room Model Class
 * Represents a room in the OceanView Reservation System
 * 
 */
public class Room {
    
    // Private fields
    private int id;
    private String roomType;
    private BigDecimal pricePerNight;
    private String status;
    
    // Room status constants
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_OCCUPIED = "OCCUPIED";
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";
    public static final String STATUS_RESERVED = "RESERVED";
    
    /**
     * Default constructor
     */
    public Room() {
    }
    
    /**
     * Constructor without ID (for new rooms)
     * 
     * @param roomType type of room
     * @param pricePerNight price per night
     * @param status room status
     */
    public Room(String roomType, BigDecimal pricePerNight, String status) {
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }
    
    /**
     * Full constructor with ID
     * 
     * @param id room ID
     * @param roomType type of room
     * @param pricePerNight price per night
     * @param status room status
     */
    public Room(int id, String roomType, BigDecimal pricePerNight, String status) {
        this.id = id;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }
    
    // Getters and Setters
    
    /**
     * Get room ID
     * @return room ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set room ID
     * @param id room ID
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get room type
     * @return room type
     */
    public String getRoomType() {
        return roomType;
    }
    
    /**
     * Set room type
     * @param roomType room type
     */
    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }
    
    /**
     * Get price per night
     * @return price per night
     */
    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }
    
    /**
     * Set price per night
     * @param pricePerNight price per night
     */
    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
    
    /**
     * Get room status
     * @return room status
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Set room status
     * @param status room status
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Check if room is available
     * @return true if room is available
     */
    public boolean isAvailable() {
        return STATUS_AVAILABLE.equalsIgnoreCase(status);
    }
    
    /**
     * Check if room is occupied
     * @return true if room is occupied
     */
    public boolean isOccupied() {
        return STATUS_OCCUPIED.equalsIgnoreCase(status);
    }
    
    /**
     * Check if room is under maintenance
     * @return true if room is under maintenance
     */
    public boolean isMaintenance() {
        return STATUS_MAINTENANCE.equalsIgnoreCase(status);
    }
    
    /**
     * Check if room is reserved
     * @return true if room is reserved
     */
    public boolean isReserved() {
        return STATUS_RESERVED.equalsIgnoreCase(status);
    }
    
    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomType='" + roomType + '\'' +
                ", pricePerNight=" + pricePerNight +
                ", status='" + status + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return id == room.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
