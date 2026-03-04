package com.oceanview.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Room {

    private int id;
    private String roomType;
    private BigDecimal pricePerNight;
    private String status;

    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_OCCUPIED = "OCCUPIED";
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";
    public static final String STATUS_RESERVED = "RESERVED";

    public Room() {
    }

    public Room(String roomType, BigDecimal pricePerNight, String status) {
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }

    public Room(int id, String roomType, BigDecimal pricePerNight, String status) {
        this.id = id;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return STATUS_AVAILABLE.equalsIgnoreCase(status);
    }

    public boolean isOccupied() {
        return STATUS_OCCUPIED.equalsIgnoreCase(status);
    }

    public boolean isMaintenance() {
        return STATUS_MAINTENANCE.equalsIgnoreCase(status);
    }

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
