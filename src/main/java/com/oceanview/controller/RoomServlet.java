package com.oceanview.controller;

import com.oceanview.exception.ValidationException;
import com.oceanview.factory.ServiceFactory;
import com.oceanview.model.Room;
import com.oceanview.service.RoomService;
import com.oceanview.util.DBConnection;
import com.oceanview.util.RBACUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "RoomServlet", urlPatterns = {"/api/rooms", "/api/rooms/*"})
public class RoomServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RoomServlet.class.getName());
    private RoomService roomService;

    @Override
    public void init() throws ServletException {
        super.init();
        roomService = ServiceFactory.getInstance().getRoomService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!RBACUtil.requireAuth(request, response)) {
            return;
        }

        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.length() > 1) {
                int roomId = Integer.parseInt(pathInfo.substring(1));
                Room room = roomService.getRoomById(roomId);

                if (room != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(roomToJson(room));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"success\": false, \"message\": \"Room not found\"}");
                }
            } else {
                String status = request.getParameter("status");
                String roomType = request.getParameter("type");
                String checkInStr = request.getParameter("checkIn");
                String checkOutStr = request.getParameter("checkOut");

                List<Room> rooms;

                if (checkInStr != null && checkOutStr != null) {
                    rooms = getDynamicallyAvailableRooms();
                } else if ("AVAILABLE".equalsIgnoreCase(status)) {
                    rooms = getDynamicallyAvailableRooms();
                } else if (status != null) {
                    rooms = roomService.getRoomsByStatus(status);
                } else if (roomType != null) {
                    rooms = roomService.getRoomsByType(roomType);
                } else {
                    rooms = roomService.getAllRooms();
                }

                response.setStatus(HttpServletResponse.SC_OK);
                out.print(roomsToJson(rooms));
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Invalid room ID\"}");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Invalid date format\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving rooms", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error retrieving rooms\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!RBACUtil.requireAuthAndRole(request, response,
                RBACUtil.ROLE_ADMIN, RBACUtil.ROLE_MANAGER)) {
            return;
        }

        PrintWriter out = response.getWriter();

        try {
            String roomType = request.getParameter("roomType");
            BigDecimal pricePerNight = new BigDecimal(request.getParameter("pricePerNight"));
            String status = request.getParameter("status");

            if (status == null || status.trim().isEmpty()) {
                status = Room.STATUS_AVAILABLE;
            }

            Room createdRoom = roomService.createRoom(roomType, pricePerNight, status);

            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print("{");
            out.print("\"success\": true,");
            out.print("\"message\": \"Room created successfully\",");
            out.print("\"room\": " + roomToJson(createdRoom));
            out.print("}");

        } catch (ValidationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"" + escapeJson(e.getMessage()) + "\"}");

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Invalid input parameters\"}");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating room", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error creating room\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!RBACUtil.requireAuthAndRole(request, response,
                RBACUtil.ROLE_ADMIN, RBACUtil.ROLE_MANAGER)) {
            return;
        }

        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"Room ID required\"}");
                return;
            }

            int roomId = Integer.parseInt(pathInfo.substring(1));

            Room room = roomService.getRoomById(roomId);
            if (room == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\": false, \"message\": \"Room not found\"}");
                return;
            }

            String roomType = request.getParameter("roomType");
            if (roomType != null) room.setRoomType(roomType);

            String priceStr = request.getParameter("pricePerNight");
            if (priceStr != null) {
                room.setPricePerNight(new BigDecimal(priceStr));
            }

            String status = request.getParameter("status");
            if (status != null) room.setStatus(status);

            boolean updated = roomService.updateRoom(room);

            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"success\": true, \"message\": \"Room updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\": false, \"message\": \"Failed to update room\"}");
            }

        } catch (ValidationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating room", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error updating room\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!RBACUtil.requireAuthAndRole(request, response,
                RBACUtil.ROLE_ADMIN, RBACUtil.ROLE_MANAGER)) {
            return;
        }

        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"Room ID required\"}");
                return;
            }

            int roomId = Integer.parseInt(pathInfo.substring(1));
            boolean deleted = roomService.deleteRoom(roomId);

            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"success\": true, \"message\": \"Room deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\": false, \"message\": \"Room not found\"}");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting room", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error deleting room\"}");
        } finally {
            out.flush();
        }
    }

    private List<Room> getDynamicallyAvailableRooms() throws Exception {
        String sql =
            "SELECT r.id, r.room_type, r.price_per_night, r.status " +
            "FROM rooms r " +
            "WHERE r.id NOT IN ( " +
            "    SELECT res.room_id FROM reservations res " +
            "    WHERE CURDATE() >= DATE(res.check_in_date) " +
            "      AND CURDATE() < DATE(res.check_out_date) " +
            ") " +
            "ORDER BY r.room_type, r.id";

        List<Room> rooms = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Room room = new Room(
                    rs.getInt("id"),
                    rs.getString("room_type"),
                    rs.getBigDecimal("price_per_night"),
                    rs.getString("status")
                );
                rooms.add(room);
            }

            return rooms;

        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException ignored) {}
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    private String roomToJson(Room r) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\": ").append(r.getId()).append(",");
        json.append("\"roomType\": \"").append(escapeJson(r.getRoomType())).append("\",");
        json.append("\"pricePerNight\": ").append(r.getPricePerNight()).append(",");
        json.append("\"status\": \"").append(escapeJson(r.getStatus())).append("\"");
        json.append("}");
        return json.toString();
    }

    private String roomsToJson(List<Room> rooms) {
        StringBuilder json = new StringBuilder();
        json.append("{\"success\": true, \"rooms\": [");

        for (int i = 0; i < rooms.size(); i++) {
            if (i > 0) json.append(",");
            json.append(roomToJson(rooms.get(i)));
        }

        json.append("], \"count\": ").append(rooms.size()).append("}");
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
