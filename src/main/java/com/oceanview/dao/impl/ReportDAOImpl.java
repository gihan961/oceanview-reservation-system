package com.oceanview.dao.impl;

import com.oceanview.dao.ReportDAO;
import com.oceanview.model.Report;
import com.oceanview.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportDAOImpl implements ReportDAO {

    private static final Logger LOGGER = Logger.getLogger(ReportDAOImpl.class.getName());
    private final DBConnection dbConnection;

    private static final String SELECT_TOTAL_REVENUE =
        "SELECT COALESCE(SUM(total_amount), 0) as total_revenue " +
        "FROM reservations " +
        "WHERE check_in_date >= ? AND check_out_date <= ?";

    private static final String SELECT_TOTAL_RESERVATIONS =
        "SELECT COUNT(*) as total_reservations " +
        "FROM reservations " +
        "WHERE check_in_date >= ? AND check_out_date <= ?";

    private static final String SELECT_OCCUPANCY_BY_ROOM_TYPE =
        "SELECT room_type, " +
        "COUNT(*) as total_rooms, " +
        "SUM(CASE WHEN status = 'OCCUPIED' OR status = 'RESERVED' THEN 1 ELSE 0 END) as occupied_rooms, " +
        "SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) as available_rooms " +
        "FROM rooms " +
        "GROUP BY room_type " +
        "ORDER BY room_type";

    private static final String SELECT_GUEST_STATISTICS =
        "SELECT guest_name, " +
        "COUNT(*) as reservation_count, " +
        "SUM(total_amount) as total_spent " +
        "FROM reservations " +
        "WHERE check_in_date >= ? AND check_out_date <= ? " +
        "GROUP BY guest_name " +
        "ORDER BY total_spent DESC " +
        "LIMIT 20";

    private static final String SELECT_RESERVATION_STATS =
        "SELECT " +
        "COUNT(*) as total_reservations, " +
        "SUM(total_amount) as total_revenue, " +
        "SUM(CASE WHEN check_out_date < CURDATE() THEN 1 ELSE 0 END) as completed, " +
        "SUM(CASE WHEN check_in_date > CURDATE() THEN 1 ELSE 0 END) as upcoming " +
        "FROM reservations " +
        "WHERE check_in_date >= ? AND check_out_date <= ?";

    public ReportDAOImpl() {
        this.dbConnection = DBConnection.getInstance();
    }

    @Override
    public boolean create(Report report) throws SQLException {

        LOGGER.log(Level.INFO, "Create not implemented for reports");
        return false;
    }

    @Override
    public boolean update(Report report) throws SQLException {

        LOGGER.log(Level.INFO, "Update not implemented for reports");
        return false;
    }

    @Override
    public boolean delete(String reportType) throws SQLException {

        LOGGER.log(Level.INFO, "Delete not implemented for reports");
        return false;
    }

    @Override
    public Report findById(int id) throws SQLException {

        LOGGER.log(Level.INFO, "FindById not implemented for reports");
        return null;
    }

    @Override
    public List<Report> findAll() throws SQLException {

        LOGGER.log(Level.INFO, "FindAll not implemented for reports");
        return new ArrayList<>();
    }

    @Override
    public Report generateFinancialReport(Date startDate, Date endDate) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();

            BigDecimal totalRevenue = BigDecimal.ZERO;
            pstmt = conn.prepareStatement(SELECT_TOTAL_REVENUE);
            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                totalRevenue = rs.getBigDecimal("total_revenue");
            }

            rs.close();
            pstmt.close();

            int totalReservations = 0;
            pstmt = conn.prepareStatement(SELECT_TOTAL_RESERVATIONS);
            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                totalReservations = rs.getInt("total_reservations");
            }

            BigDecimal averageReservationValue = BigDecimal.ZERO;
            if (totalReservations > 0) {
                averageReservationValue = totalRevenue.divide(
                    BigDecimal.valueOf(totalReservations),
                    2,
                    java.math.RoundingMode.HALF_UP
                );
            }

            Report report = new Report(
                "FINANCIAL",
                startDate,
                endDate,
                totalRevenue,
                totalReservations,
                averageReservationValue
            );

            LOGGER.log(Level.INFO, "Financial report generated for {0} to {1}",
                      new Object[]{startDate, endDate});
            return report;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating financial report", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Report> generateOccupancyReport() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Report> reports = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_OCCUPANCY_BY_ROOM_TYPE);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Report report = new Report(
                    rs.getString("room_type"),
                    rs.getInt("total_rooms"),
                    rs.getInt("occupied_rooms"),
                    rs.getInt("available_rooms")
                );
                reports.add(report);
            }

            LOGGER.log(Level.INFO, "Occupancy report generated with {0} room types",
                      reports.size());
            return reports;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating occupancy report", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Report> generateGuestStatisticsReport(Date startDate, Date endDate)
            throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Report> reports = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_GUEST_STATISTICS);

            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Report report = new Report();
                report.setReportType("GUEST_STATISTICS");
                report.setStartDate(startDate);
                report.setEndDate(endDate);
                report.setGuestName(rs.getString("guest_name"));
                report.setReservationCount(rs.getInt("reservation_count"));
                report.setTotalSpent(rs.getBigDecimal("total_spent"));
                reports.add(report);
            }

            LOGGER.log(Level.INFO, "Guest statistics report generated for {0} guests",
                      reports.size());
            return reports;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating guest statistics report", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public BigDecimal getTotalRevenue(Date startDate, Date endDate) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_TOTAL_REVENUE);

            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                BigDecimal revenue = rs.getBigDecimal("total_revenue");
                LOGGER.log(Level.INFO, "Total revenue calculated: {0}", revenue);
                return revenue;
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total revenue", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public int getTotalReservations(Date startDate, Date endDate) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_TOTAL_RESERVATIONS);

            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("total_reservations");
                LOGGER.log(Level.INFO, "Total reservations counted: {0}", count);
                return count;
            }

            return 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total reservations", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public Report getReservationStatistics(Date startDate, Date endDate) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_RESERVATION_STATS);

            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                Report report = new Report("STATISTICS", startDate, endDate);
                report.setTotalReservations(rs.getInt("total_reservations"));
                report.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                report.setCompletedReservations(rs.getInt("completed"));
                report.setUpcomingReservations(rs.getInt("upcoming"));

                if (report.getTotalReservations() > 0) {
                    report.calculateAverageReservationValue();
                }

                LOGGER.log(Level.INFO, "Reservation statistics generated");
                return report;
            }

            return null;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting reservation statistics", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database resources", e);
        }
    }
}
