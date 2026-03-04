package com.oceanview.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Objects;

public class Report {

    private String reportType;
    private Date startDate;
    private Date endDate;
    private String generatedBy;
    private Date generatedDate;

    private BigDecimal totalRevenue;
    private int totalReservations;
    private BigDecimal averageReservationValue;

    private String roomType;
    private int totalRooms;
    private int occupiedRooms;
    private int availableRooms;
    private double occupancyRate;

    private String guestName;
    private int reservationCount;
    private BigDecimal totalSpent;

    private int cancelledReservations;
    private int completedReservations;
    private int upcomingReservations;

    public Report() {
    }

    public Report(String reportType, Date startDate, Date endDate) {
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.generatedDate = new Date(System.currentTimeMillis());
    }

    public Report(String reportType, Date startDate, Date endDate,
                 BigDecimal totalRevenue, int totalReservations,
                 BigDecimal averageReservationValue) {
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalRevenue = totalRevenue;
        this.totalReservations = totalReservations;
        this.averageReservationValue = averageReservationValue;
        this.generatedDate = new Date(System.currentTimeMillis());
    }

    public Report(String roomType, int totalRooms, int occupiedRooms, int availableRooms) {
        this.reportType = "OCCUPANCY";
        this.roomType = roomType;
        this.totalRooms = totalRooms;
        this.occupiedRooms = occupiedRooms;
        this.availableRooms = availableRooms;
        this.occupancyRate = totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0;
        this.generatedDate = new Date(System.currentTimeMillis());
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public Date getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(Date generatedDate) {
        this.generatedDate = generatedDate;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalReservations() {
        return totalReservations;
    }

    public void setTotalReservations(int totalReservations) {
        this.totalReservations = totalReservations;
    }

    public BigDecimal getAverageReservationValue() {
        return averageReservationValue;
    }

    public void setAverageReservationValue(BigDecimal averageReservationValue) {
        this.averageReservationValue = averageReservationValue;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public int getOccupiedRooms() {
        return occupiedRooms;
    }

    public void setOccupiedRooms(int occupiedRooms) {
        this.occupiedRooms = occupiedRooms;
    }

    public int getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(int availableRooms) {
        this.availableRooms = availableRooms;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public int getReservationCount() {
        return reservationCount;
    }

    public void setReservationCount(int reservationCount) {
        this.reservationCount = reservationCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public int getCancelledReservations() {
        return cancelledReservations;
    }

    public void setCancelledReservations(int cancelledReservations) {
        this.cancelledReservations = cancelledReservations;
    }

    public int getCompletedReservations() {
        return completedReservations;
    }

    public void setCompletedReservations(int completedReservations) {
        this.completedReservations = completedReservations;
    }

    public int getUpcomingReservations() {
        return upcomingReservations;
    }

    public void setUpcomingReservations(int upcomingReservations) {
        this.upcomingReservations = upcomingReservations;
    }

    public void calculateOccupancyRate() {
        if (totalRooms > 0) {
            this.occupancyRate = (double) occupiedRooms / totalRooms * 100;
        } else {
            this.occupancyRate = 0;
        }
    }

    public void calculateAverageReservationValue() {
        if (totalReservations > 0 && totalRevenue != null) {
            this.averageReservationValue = totalRevenue.divide(
                BigDecimal.valueOf(totalReservations),
                2,
                java.math.RoundingMode.HALF_UP
            );
        } else {
            this.averageReservationValue = BigDecimal.ZERO;
        }
    }

    @Override
    public String toString() {
        return "Report{" +
                "reportType='" + reportType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", generatedBy='" + generatedBy + '\'' +
                ", generatedDate=" + generatedDate +
                ", totalRevenue=" + totalRevenue +
                ", totalReservations=" + totalReservations +
                ", averageReservationValue=" + averageReservationValue +
                ", roomType='" + roomType + '\'' +
                ", totalRooms=" + totalRooms +
                ", occupiedRooms=" + occupiedRooms +
                ", availableRooms=" + availableRooms +
                ", occupancyRate=" + occupancyRate +
                ", guestName='" + guestName + '\'' +
                ", reservationCount=" + reservationCount +
                ", totalSpent=" + totalSpent +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Objects.equals(reportType, report.reportType) &&
               Objects.equals(startDate, report.startDate) &&
               Objects.equals(endDate, report.endDate) &&
               Objects.equals(generatedDate, report.generatedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportType, startDate, endDate, generatedDate);
    }
}
