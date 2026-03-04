package com.oceanview.dao.impl;

import com.oceanview.dao.ReservationDAO;
import com.oceanview.model.Reservation;
import com.oceanview.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationDAOImpl implements ReservationDAO {

    private static final Logger LOGGER = Logger.getLogger(ReservationDAOImpl.class.getName());
    private final DBConnection dbConnection;

    private static final String INSERT_RESERVATION =
        "INSERT INTO reservations (reservation_number, guest_name, address, contact_number, " +
        "room_id, check_in_date, check_out_date, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_RESERVATION =
        "UPDATE reservations SET reservation_number = ?, guest_name = ?, address = ?, " +
        "contact_number = ?, room_id = ?, check_in_date = ?, check_out_date = ?, " +
        "total_amount = ? WHERE id = ?";

    private static final String DELETE_RESERVATION =
        "DELETE FROM reservations WHERE id = ?";

    private static final String SELECT_RESERVATION_BY_ID =
        "SELECT r.id, r.reservation_number, r.guest_name, r.address, r.contact_number, " +
        "r.room_id, r.check_in_date, r.check_out_date, r.total_amount, r.created_at, " +
        "rm.room_type, rm.price_per_night " +
        "FROM reservations r " +
        "LEFT JOIN rooms rm ON r.room_id = rm.id " +
        "WHERE r.id = ?";

    private static final String SELECT_ALL_RESERVATIONS =
        "SELECT r.id, r.reservation_number, r.guest_name, r.address, r.contact_number, " +
        "r.room_id, r.check_in_date, r.check_out_date, r.total_amount, r.created_at, " +
        "rm.room_type, rm.price_per_night " +
        "FROM reservations r " +
        "LEFT JOIN rooms rm ON r.room_id = rm.id " +
        "ORDER BY r.created_at DESC";

    private static final String SELECT_RESERVATION_BY_NUMBER =
        "SELECT r.id, r.reservation_number, r.guest_name, r.address, r.contact_number, " +
        "r.room_id, r.check_in_date, r.check_out_date, r.total_amount, r.created_at, " +
        "rm.room_type, rm.price_per_night " +
        "FROM reservations r " +
        "LEFT JOIN rooms rm ON r.room_id = rm.id " +
        "WHERE r.reservation_number = ?";

    private static final String SELECT_RESERVATIONS_BY_GUEST_NAME =
        "SELECT r.id, r.reservation_number, r.guest_name, r.address, r.contact_number, " +
        "r.room_id, r.check_in_date, r.check_out_date, r.total_amount, r.created_at, " +
        "rm.room_type, rm.price_per_night " +
        "FROM reservations r " +
        "LEFT JOIN rooms rm ON r.room_id = rm.id " +
        "WHERE r.guest_name LIKE ? " +
        "ORDER BY r.created_at DESC";

    private static final String SELECT_RESERVATIONS_BY_ROOM_ID =
        "SELECT r.id, r.reservation_number, r.guest_name, r.address, r.contact_number, " +
        "r.room_id, r.check_in_date, r.check_out_date, r.total_amount, r.created_at, " +
        "rm.room_type, rm.price_per_night " +
        "FROM reservations r " +
        "LEFT JOIN rooms rm ON r.room_id = rm.id " +
        "WHERE r.room_id = ? " +
        "ORDER BY r.check_in_date DESC";

    private static final String SELECT_RESERVATIONS_BY_DATE_RANGE =
        "SELECT r.id, r.reservation_number, r.guest_name, r.address, r.contact_number, " +
        "r.room_id, r.check_in_date, r.check_out_date, r.total_amount, r.created_at, " +
        "rm.room_type, rm.price_per_night " +
        "FROM reservations r " +
        "LEFT JOIN rooms rm ON r.room_id = rm.id " +
        "WHERE r.check_in_date >= ? AND r.check_out_date <= ? " +
        "ORDER BY r.check_in_date";

    private static final String SELECT_CHECK_INS_TODAY =
        "SELECT r.id, r.reservation_number, r.guest_name, r.address, r.contact_number, " +
        "r.room_id, r.check_in_date, r.check_out_date, r.total_amount, r.created_at, " +
        "rm.room_type, rm.price_per_night " +
        "FROM reservations r " +
        "LEFT JOIN rooms rm ON r.room_id = rm.id " +
        "WHERE r.check_in_date = CURDATE() " +
        "ORDER BY r.guest_name";

    private static final String SELECT_CHECK_OUTS_TODAY =
        "SELECT r.id, r.reservation_number, r.guest_name, r.address, r.contact_number, " +
        "r.room_id, r.check_in_date, r.check_out_date, r.total_amount, r.created_at, " +
        "rm.room_type, rm.price_per_night " +
        "FROM reservations r " +
        "LEFT JOIN rooms rm ON r.room_id = rm.id " +
        "WHERE r.check_out_date = CURDATE() " +
        "ORDER BY r.guest_name";

    public ReservationDAOImpl() {
        this.dbConnection = DBConnection.getInstance();
    }

    @Override
    public boolean create(Reservation reservation) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(INSERT_RESERVATION, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, reservation.getReservationNumber());
            pstmt.setString(2, reservation.getGuestName());
            pstmt.setString(3, reservation.getAddress());
            pstmt.setString(4, reservation.getContactNumber());
            pstmt.setInt(5, reservation.getRoomId());
            pstmt.setDate(6, reservation.getCheckInDate());
            pstmt.setDate(7, reservation.getCheckOutDate());
            pstmt.setBigDecimal(8, reservation.getTotalAmount());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    reservation.setId(rs.getInt(1));
                }
                LOGGER.log(Level.INFO, "Reservation created successfully: {0}",
                          reservation.getReservationNumber());
                return true;
            }

            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating reservation: " +
                      reservation.getReservationNumber(), e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public boolean update(Reservation reservation) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(UPDATE_RESERVATION);

            pstmt.setString(1, reservation.getReservationNumber());
            pstmt.setString(2, reservation.getGuestName());
            pstmt.setString(3, reservation.getAddress());
            pstmt.setString(4, reservation.getContactNumber());
            pstmt.setInt(5, reservation.getRoomId());
            pstmt.setDate(6, reservation.getCheckInDate());
            pstmt.setDate(7, reservation.getCheckOutDate());
            pstmt.setBigDecimal(8, reservation.getTotalAmount());
            pstmt.setInt(9, reservation.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Reservation updated successfully: {0}",
                          reservation.getReservationNumber());
                return true;
            }

            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating reservation: " +
                      reservation.getReservationNumber(), e);
            throw e;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(DELETE_RESERVATION);

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Reservation deleted successfully: ID {0}", id);
                return true;
            }

            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting reservation: ID " + id, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    @Override
    public Reservation findById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_RESERVATION_BY_ID);

            pstmt.setInt(1, id);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractReservationFromResultSet(rs);
            }

            return null;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding reservation by ID: " + id, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Reservation> findAll() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_ALL_RESERVATIONS);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }

            LOGGER.log(Level.INFO, "Found {0} reservations", reservations.size());
            return reservations;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all reservations", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public Reservation findByReservationNumber(String reservationNumber) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_RESERVATION_BY_NUMBER);

            pstmt.setString(1, reservationNumber);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractReservationFromResultSet(rs);
            }

            return null;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding reservation by number: " +
                      reservationNumber, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Reservation> findByGuestName(String guestName) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_RESERVATIONS_BY_GUEST_NAME);

            pstmt.setString(1, "%" + guestName + "%");

            rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }

            LOGGER.log(Level.INFO, "Found {0} reservations for guest: {1}",
                      new Object[]{reservations.size(), guestName});
            return reservations;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding reservations by guest name: " +
                      guestName, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Reservation> findByRoomId(int roomId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_RESERVATIONS_BY_ROOM_ID);

            pstmt.setInt(1, roomId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }

            LOGGER.log(Level.INFO, "Found {0} reservations for room ID: {1}",
                      new Object[]{reservations.size(), roomId});
            return reservations;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding reservations by room ID: " + roomId, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Reservation> findByDateRange(Date startDate, Date endDate) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_RESERVATIONS_BY_DATE_RANGE);

            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }

            LOGGER.log(Level.INFO, "Found {0} reservations in date range", reservations.size());
            return reservations;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding reservations by date range", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Reservation> findCheckInsToday() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_CHECK_INS_TODAY);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }

            LOGGER.log(Level.INFO, "Found {0} check-ins today", reservations.size());
            return reservations;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding check-ins today", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Reservation> findCheckOutsToday() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_CHECK_OUTS_TODAY);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }

            LOGGER.log(Level.INFO, "Found {0} check-outs today", reservations.size());
            return reservations;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding check-outs today", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    private Reservation extractReservationFromResultSet(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation(
            rs.getInt("id"),
            rs.getString("reservation_number"),
            rs.getString("guest_name"),
            rs.getString("address"),
            rs.getString("contact_number"),
            rs.getInt("room_id"),
            rs.getDate("check_in_date"),
            rs.getDate("check_out_date"),
            rs.getBigDecimal("total_amount"),
            rs.getTimestamp("created_at")
        );

        reservation.setRoomType(rs.getString("room_type"));
        reservation.setPricePerNight(rs.getBigDecimal("price_per_night"));

        return reservation;
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
