package com.oceanview.service;

import com.oceanview.dao.ReportDAO;
import com.oceanview.dao.impl.ReportDAOImpl;
import com.oceanview.exception.ValidationException;
import com.oceanview.model.Report;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Report Service
 * Handles report generation business logic
 * 
 */
public class ReportService {
    
    private static final Logger LOGGER = Logger.getLogger(ReportService.class.getName());
    private final ReportDAO reportDAO;
    
    /**
     * Constructor
     */
    public ReportService() {
        this.reportDAO = new ReportDAOImpl();
    }
    
    /**
     * Constructor with custom DAO (for testing)
     * 
     * @param reportDAO Report DAO implementation
     */
    public ReportService(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }
    
    /**
     * Generate financial report for date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Financial report
     * @throws ValidationException if validation fails
     */
    public Report generateFinancialReport(Date startDate, Date endDate) 
            throws ValidationException {
        
        validateDateRange(startDate, endDate);
        
        try {
            Report report = reportDAO.generateFinancialReport(startDate, endDate);
            
            if (report == null) {
                report = new Report("FINANCIAL", startDate, endDate);
                report.setTotalRevenue(BigDecimal.ZERO);
                report.setTotalReservations(0);
                report.setAverageReservationValue(BigDecimal.ZERO);
            }
            
            LOGGER.log(Level.INFO, "Financial report generated for {0} to {1}", 
                      new Object[]{startDate, endDate});
            return report;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating financial report", e);
            throw new ValidationException("Failed to generate financial report: " + e.getMessage());
        }
    }
    
    /**
     * Generate room occupancy report
     * 
     * @return List of occupancy reports by room type
     * @throws ValidationException if operation fails
     */
    public List<Report> generateOccupancyReport() throws ValidationException {
        
        try {
            List<Report> reports = reportDAO.generateOccupancyReport();
            
            LOGGER.log(Level.INFO, "Occupancy report generated with {0} room types", 
                      reports.size());
            return reports;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating occupancy report", e);
            throw new ValidationException("Failed to generate occupancy report: " + e.getMessage());
        }
    }
    
    /**
     * Generate guest statistics report
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return List of guest statistics
     * @throws ValidationException if validation fails
     */
    public List<Report> generateGuestStatisticsReport(Date startDate, Date endDate) 
            throws ValidationException {
        
        validateDateRange(startDate, endDate);
        
        try {
            List<Report> reports = reportDAO.generateGuestStatisticsReport(startDate, endDate);
            
            LOGGER.log(Level.INFO, "Guest statistics report generated for {0} guests", 
                      reports.size());
            return reports;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating guest statistics report", e);
            throw new ValidationException("Failed to generate guest statistics: " + e.getMessage());
        }
    }
    
    /**
     * Get total revenue for date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Total revenue
     * @throws ValidationException if validation fails
     */
    public BigDecimal getTotalRevenue(Date startDate, Date endDate) 
            throws ValidationException {
        
        validateDateRange(startDate, endDate);
        
        try {
            BigDecimal revenue = reportDAO.getTotalRevenue(startDate, endDate);
            
            LOGGER.log(Level.INFO, "Total revenue calculated: {0}", revenue);
            return revenue != null ? revenue : BigDecimal.ZERO;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting total revenue", e);
            throw new ValidationException("Failed to calculate total revenue: " + e.getMessage());
        }
    }
    
    /**
     * Get total reservations count for date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Total reservations count
     * @throws ValidationException if validation fails
     */
    public int getTotalReservations(Date startDate, Date endDate) 
            throws ValidationException {
        
        validateDateRange(startDate, endDate);
        
        try {
            int count = reportDAO.getTotalReservations(startDate, endDate);
            
            LOGGER.log(Level.INFO, "Total reservations counted: {0}", count);
            return count;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting total reservations", e);
            throw new ValidationException("Failed to count reservations: " + e.getMessage());
        }
    }
    
    /**
     * Get reservation statistics
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Report with reservation statistics
     * @throws ValidationException if validation fails
     */
    public Report getReservationStatistics(Date startDate, Date endDate) 
            throws ValidationException {
        
        validateDateRange(startDate, endDate);
        
        try {
            Report report = reportDAO.getReservationStatistics(startDate, endDate);
            
            if (report == null) {
                report = new Report("STATISTICS", startDate, endDate);
                report.setTotalReservations(0);
                report.setTotalRevenue(BigDecimal.ZERO);
                report.setCompletedReservations(0);
                report.setUpcomingReservations(0);
            }
            
            LOGGER.log(Level.INFO, "Reservation statistics generated");
            return report;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting reservation statistics", e);
            throw new ValidationException("Failed to get statistics: " + e.getMessage());
        }
    }
    
    /**
     * Generate monthly report
     * 
     * @param year Year
     * @param month Month (1-12)
     * @return Monthly financial report
     * @throws ValidationException if validation fails
     */
    public Report generateMonthlyReport(int year, int month) throws ValidationException {
        
        if (year < 2000 || year > 2100) {
            throw new ValidationException("Invalid year");
        }
        
        if (month < 1 || month > 12) {
            throw new ValidationException("Invalid month. Must be between 1 and 12");
        }
        
        // Calculate start and end dates for the month
        LocalDate startLocal = LocalDate.of(year, month, 1);
        LocalDate endLocal = startLocal.plusMonths(1).minusDays(1);
        
        Date startDate = Date.valueOf(startLocal);
        Date endDate = Date.valueOf(endLocal);
        
        return generateFinancialReport(startDate, endDate);
    }
    
    /**
     * Generate yearly report
     * 
     * @param year Year
     * @return Yearly financial report
     * @throws ValidationException if validation fails
     */
    public Report generateYearlyReport(int year) throws ValidationException {
        
        if (year < 2000 || year > 2100) {
            throw new ValidationException("Invalid year");
        }
        
        // Calculate start and end dates for the year
        LocalDate startLocal = LocalDate.of(year, 1, 1);
        LocalDate endLocal = LocalDate.of(year, 12, 31);
        
        Date startDate = Date.valueOf(startLocal);
        Date endDate = Date.valueOf(endLocal);
        
        return generateFinancialReport(startDate, endDate);
    }
    
    /**
     * Generate today's report
     * 
     * @return Today's financial report
     * @throws ValidationException if operation fails
     */
    public Report generateTodayReport() throws ValidationException {
        
        LocalDate today = LocalDate.now();
        Date todayDate = Date.valueOf(today);
        
        return generateFinancialReport(todayDate, todayDate);
    }
    
    /**
     * Generate this week's report
     * 
     * @return This week's financial report
     * @throws ValidationException if operation fails
     */
    public Report generateWeeklyReport() throws ValidationException {
        
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        Date startDate = Date.valueOf(weekStart);
        Date endDate = Date.valueOf(weekEnd);
        
        return generateFinancialReport(startDate, endDate);
    }
    
    /**
     * Generate this month's report
     * 
     * @return This month's financial report
     * @throws ValidationException if operation fails
     */
    public Report generateCurrentMonthReport() throws ValidationException {
        
        LocalDate today = LocalDate.now();
        return generateMonthlyReport(today.getYear(), today.getMonthValue());
    }
    
    /**
     * Generate this year's report
     * 
     * @return This year's financial report
     * @throws ValidationException if operation fails
     */
    public Report generateCurrentYearReport() throws ValidationException {
        
        LocalDate today = LocalDate.now();
        return generateYearlyReport(today.getYear());
    }
    
    /**
     * Calculate average occupancy rate
     * 
     * @return Average occupancy rate as percentage
     * @throws ValidationException if calculation fails
     */
    public double calculateAverageOccupancyRate() throws ValidationException {
        
        try {
            List<Report> occupancyReports = reportDAO.generateOccupancyReport();
            
            if (occupancyReports == null || occupancyReports.isEmpty()) {
                return 0.0;
            }
            
            double totalRate = 0.0;
            for (Report report : occupancyReports) {
                totalRate += report.getOccupancyRate();
            }
            
            double averageRate = totalRate / occupancyReports.size();
            
            LOGGER.log(Level.INFO, "Average occupancy rate calculated: {0}%", averageRate);
            return averageRate;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating average occupancy rate", e);
            throw new ValidationException("Failed to calculate occupancy rate: " + e.getMessage());
        }
    }
    
    /**
     * Validate date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @throws ValidationException if validation fails
     */
    private void validateDateRange(Date startDate, Date endDate) throws ValidationException {
        
        if (startDate == null) {
            throw new ValidationException("Start date cannot be null");
        }
        
        if (endDate == null) {
            throw new ValidationException("End date cannot be null");
        }
        
        if (endDate.before(startDate)) {
            throw new ValidationException("End date must be after start date");
        }
        
        // Check if date range is too large (e.g., max 5 years)
        LocalDate start = startDate.toLocalDate();
        LocalDate end = endDate.toLocalDate();
        long years = java.time.temporal.ChronoUnit.YEARS.between(start, end);
        
        if (years > 5) {
            throw new ValidationException("Date range cannot exceed 5 years");
        }
    }
}
