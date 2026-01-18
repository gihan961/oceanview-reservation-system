package com.oceanview.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Reservation Model Class
 * Represents a reservation in the OceanView Reservation System
 * 
 */
public class Reservation {
    
    // Private fields
    private int id;
    private String reservationNumber;
    private String guestName;
    private String address;
    private String contactNumber;
    private int roomId;
    private Date checkInDate;
    private Date checkOutDate;
    private BigDecimal totalAmount;
    private Timestamp createdAt;
    
    // Additional fields for joined data (not in database)
    private String roomType;
    private BigDecimal pricePerNight;
    
    /**
     * Default constructor
     */
    public Reservation() {
    }
    
    /**
     * Constructor without ID (for new reservations)
     * 
     * @param reservationNumber unique reservation number
     * @param guestName guest's name
     * @param address guest's address
     * @param contactNumber guest's contact number
     * @param roomId room ID
     * @param checkInDate check-in date
     * @param checkOutDate check-out date
     * @param totalAmount total amount
     */
    public Reservation(String reservationNumber, String guestName, String address, 
                      String contactNumber, int roomId, Date checkInDate, 
                      Date checkOutDate, BigDecimal totalAmount) {
        this.reservationNumber = reservationNumber;
        this.guestName = guestName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalAmount = totalAmount;
    }
    
    /**
     * Full constructor with ID
     * 
     * @param id reservation ID
     * @param reservationNumber unique reservation number
     * @param guestName guest's name
     * @param address guest's address
     * @param contactNumber guest's contact number
     * @param roomId room ID
     * @param checkInDate check-in date
     * @param checkOutDate check-out date
     * @param totalAmount total amount
     * @param createdAt creation timestamp
     */
    public Reservation(int id, String reservationNumber, String guestName, String address,
                      String contactNumber, int roomId, Date checkInDate,
                      Date checkOutDate, BigDecimal totalAmount, Timestamp createdAt) {
        this.id = id;
        this.reservationNumber = reservationNumber;
        this.guestName = guestName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    
    /**
     * Get reservation ID
     * @return reservation ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set reservation ID
     * @param id reservation ID
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get reservation number
     * @return reservation number
     */
    public String getReservationNumber() {
        return reservationNumber;
    }
    
    /**
     * Set reservation number
     * @param reservationNumber reservation number
     */
    public void setReservationNumber(String reservationNumber) {
        this.reservationNumber = reservationNumber;
    }
    
    /**
     * Get guest name
     * @return guest name
     */
    public String getGuestName() {
        return guestName;
    }
    
    /**
     * Set guest name
     * @param guestName guest name
     */
    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }
    
    /**
     * Get address
     * @return address
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * Set address
     * @param address address
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * Get contact number
     * @return contact number
     */
    public String getContactNumber() {
        return contactNumber;
    }
    
    /**
     * Set contact number
     * @param contactNumber contact number
     */
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    /**
     * Get room ID
     * @return room ID
     */
    public int getRoomId() {
        return roomId;
    }
    
    /**
     * Set room ID
     * @param roomId room ID
     */
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    
    /**
     * Get check-in date
     * @return check-in date
     */
    public Date getCheckInDate() {
        return checkInDate;
    }
    
    /**
     * Set check-in date
     * @param checkInDate check-in date
     */
    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }
    
    /**
     * Get check-out date
     * @return check-out date
     */
    public Date getCheckOutDate() {
        return checkOutDate;
    }
    
    /**
     * Set check-out date
     * @param checkOutDate check-out date
     */
    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
    
    /**
     * Get total amount
     * @return total amount
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    /**
     * Set total amount
     * @param totalAmount total amount
     */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    /**
     * Get creation timestamp
     * @return creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Set creation timestamp
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Get room type (for joined queries)
     * @return room type
     */
    public String getRoomType() {
        return roomType;
    }
    
    /**
     * Set room type (for joined queries)
     * @param roomType room type
     */
    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }
    
    /**
     * Get price per night (for joined queries)
     * @return price per night
     */
    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }
    
    /**
     * Set price per night (for joined queries)
     * @param pricePerNight price per night
     */
    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
    
    /**
     * Calculate number of nights
     * @return number of nights
     */
    public long getNumberOfNights() {
        if (checkInDate != null && checkOutDate != null) {
            return ChronoUnit.DAYS.between(
                checkInDate.toLocalDate(), 
                checkOutDate.toLocalDate()
            );
        }
        return 0;
    }
    
    /**
     * Check if reservation is for today
     * @return true if check-in is today
     */
    public boolean isCheckInToday() {
        if (checkInDate != null) {
            return checkInDate.toLocalDate().equals(java.time.LocalDate.now());
        }
        return false;
    }
    
    /**
     * Check if reservation checkout is today
     * @return true if check-out is today
     */
    public boolean isCheckOutToday() {
        if (checkOutDate != null) {
            return checkOutDate.toLocalDate().equals(java.time.LocalDate.now());
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", reservationNumber='" + reservationNumber + '\'' +
                ", guestName='" + guestName + '\'' +
                ", address='" + address + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", roomId=" + roomId +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", totalAmount=" + totalAmount +
                ", createdAt=" + createdAt +
                ", numberOfNights=" + getNumberOfNights() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id == that.id && Objects.equals(reservationNumber, that.reservationNumber);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, reservationNumber);
    }
}
