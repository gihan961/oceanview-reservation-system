package com.oceanview.dao.impl;

import com.oceanview.dao.RoomDAO;
import com.oceanview.model.Room;
import com.oceanview.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomDAOImpl implements RoomDAO {

    private static final Logger LOGGER = Logger.getLogger(RoomDAOImpl.class.getName());
    private final DBConnection dbConnection;

    private static final String INSERT_ROOM =
        "INSERT INTO rooms (room_type, price_per_night, status) VALUES (?, ?, ?)";

    private static final String UPDATE_ROOM =
        "UPDATE rooms SET room_type = ?, price_per_night = ?, status = ? WHERE id = ?";

    private static final String DELETE_ROOM =
        "DELETE FROM rooms WHERE id = ?";

    private static final String SELECT_ROOM_BY_ID =
        "SELECT id, room_type, price_per_night, status FROM rooms WHERE id = ?";

    private static final String SELECT_ALL_ROOMS =
        "SELECT id, room_type, price_per_night, status FROM rooms ORDER BY id";

    private static final String SELECT_ROOMS_BY_STATUS =
        "SELECT id, room_type, price_per_night, status FROM rooms WHERE status = ? ORDER BY room_type, id";

    private static final String SELECT_ROOMS_BY_TYPE =
        "SELECT id, room_type, price_per_night, status FROM rooms WHERE room_type = ? ORDER BY id";

    private static final String UPDATE_ROOM_STATUS =
        "UPDATE rooms SET status = ? WHERE id = ?";

    public RoomDAOImpl() {
        this.dbConnection = DBConnection.getInstance();
    }

    @Override
    public boolean create(Room room) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(INSERT_ROOM, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, room.getRoomType());
            pstmt.setBigDecimal(2, room.getPricePerNight());
            pstmt.setString(3, room.getStatus());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    room.setId(rs.getInt(1));
                }
                LOGGER.log(Level.INFO, "Room created successfully: {0}", room.getRoomType());
                return true;
            }

            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating room: " + room.getRoomType(), e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public boolean update(Room room) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(UPDATE_ROOM);

            pstmt.setString(1, room.getRoomType());
            pstmt.setBigDecimal(2, room.getPricePerNight());
            pstmt.setString(3, room.getStatus());
            pstmt.setInt(4, room.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Room updated successfully: ID {0}", room.getId());
                return true;
            }

            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating room: ID " + room.getId(), e);
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
            pstmt = conn.prepareStatement(DELETE_ROOM);

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Room deleted successfully: ID {0}", id);
                return true;
            }

            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting room: ID " + id, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    @Override
    public Room findById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_ROOM_BY_ID);

            pstmt.setInt(1, id);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractRoomFromResultSet(rs);
            }

            return null;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding room by ID: " + id, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Room> findAll() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Room> rooms = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_ALL_ROOMS);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }

            LOGGER.log(Level.INFO, "Found {0} rooms", rooms.size());
            return rooms;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all rooms", e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Room> findByStatus(String status) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Room> rooms = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_ROOMS_BY_STATUS);

            pstmt.setString(1, status);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }

            LOGGER.log(Level.INFO, "Found {0} rooms with status: {1}", new Object[]{rooms.size(), status});
            return rooms;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding rooms by status: " + status, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<Room> findByRoomType(String roomType) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Room> rooms = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_ROOMS_BY_TYPE);

            pstmt.setString(1, roomType);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }

            LOGGER.log(Level.INFO, "Found {0} rooms of type: {1}", new Object[]{rooms.size(), roomType});
            return rooms;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding rooms by type: " + roomType, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public boolean updateStatus(int roomId, String status) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(UPDATE_ROOM_STATUS);

            pstmt.setString(1, status);
            pstmt.setInt(2, roomId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Room status updated successfully: ID {0} to {1}",
                          new Object[]{roomId, status});
                return true;
            }

            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating room status: ID " + roomId, e);
            throw e;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    private Room extractRoomFromResultSet(ResultSet rs) throws SQLException {
        return new Room(
            rs.getInt("id"),
            rs.getString("room_type"),
            rs.getBigDecimal("price_per_night"),
            rs.getString("status")
        );
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
