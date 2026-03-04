package com.oceanview.dao;

import com.oceanview.model.Report;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface ReportDAO {

    boolean create(Report report) throws SQLException;

    boolean update(Report report) throws SQLException;

    boolean delete(String reportType) throws SQLException;

    Report findById(int id) throws SQLException;

    List<Report> findAll() throws SQLException;

    Report generateFinancialReport(Date startDate, Date endDate) throws SQLException;

    List<Report> generateOccupancyReport() throws SQLException;

    List<Report> generateGuestStatisticsReport(Date startDate, Date endDate) throws SQLException;

    BigDecimal getTotalRevenue(Date startDate, Date endDate) throws SQLException;

    int getTotalReservations(Date startDate, Date endDate) throws SQLException;

    Report getReservationStatistics(Date startDate, Date endDate) throws SQLException;
}
