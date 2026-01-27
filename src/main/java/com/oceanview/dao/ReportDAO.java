package com.oceanview.dao;

import com.oceanview.model.Report;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * Report Data Access Object Interface
 * Defines operations for generating various reports
 * 
 */
public interface ReportDAO {
    
    /**
     * Create a new report record
     * 
     * @param report Report object to create
     * @return true if creation successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean create(Report report) throws SQLException;
    
    /**
     * Update an existing report
     * 
     * @param report Report object with updated information
     * @return true if update successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean update(Report report) throws SQLException;
    
    /**
     * Delete a report by type
     * 
     * @param reportType Report type
     * @return true if deletion successful, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean delete(String reportType) throws SQLException;
    
    /**
     * Find report by ID (not applicable for this implementation)
     * 
     * @param id Report ID
     * @return Report object if found, null otherwise
     * @throws SQLException if database error occurs
     */
    Report findById(int id) throws SQLException;
    
    /**
     * Find all reports
     * 
     * @return List of all reports
     * @throws SQLException if database error occurs
     */
    List<Report> findAll() throws SQLException;
    
    /**
     * Generate financial report for date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Report object with financial data
     * @throws SQLException if database error occurs
     */
    Report generateFinancialReport(Date startDate, Date endDate) throws SQLException;
    
    /**
     * Generate room occupancy report
     * 
     * @return List of occupancy reports by room type
     * @throws SQLException if database error occurs
     */
    List<Report> generateOccupancyReport() throws SQLException;
    
    /**
     * Generate guest statistics report
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return List of guest statistics
     * @throws SQLException if database error occurs
     */
    List<Report> generateGuestStatisticsReport(Date startDate, Date endDate) throws SQLException;
    
    /**
     * Get total revenue for date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Total revenue
     * @throws SQLException if database error occurs
     */
    BigDecimal getTotalRevenue(Date startDate, Date endDate) throws SQLException;
    
    /**
     * Get total reservations count for date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Total reservations count
     * @throws SQLException if database error occurs
     */
    int getTotalReservations(Date startDate, Date endDate) throws SQLException;
    
    /**
     * Get reservation statistics by date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Report with reservation statistics
     * @throws SQLException if database error occurs
     */
    Report getReservationStatistics(Date startDate, Date endDate) throws SQLException;
}
