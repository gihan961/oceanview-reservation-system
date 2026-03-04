package com.oceanview.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Reservation {

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

    private String roomType;
    private BigDecimal pricePerNight;

    public Reservation() {
    }

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReservationNumber() {
        return reservationNumber;
    }

    public void setReservationNumber(String reservationNumber) {
        this.reservationNumber = reservationNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    public long getNumberOfNights() {
        if (checkInDate != null && checkOutDate != null) {
            return ChronoUnit.DAYS.between(
                checkInDate.toLocalDate(),
                checkOutDate.toLocalDate()
            );
        }
        return 0;
    }

    public boolean isCheckInToday() {
        if (checkInDate != null) {
            return checkInDate.toLocalDate().equals(java.time.LocalDate.now());
        }
        return false;
    }

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
