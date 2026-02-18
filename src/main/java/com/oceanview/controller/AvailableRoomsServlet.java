package com.oceanview.controller;

import com.oceanview.util.DBConnection;
import com.oceanview.util.RBACUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "AvailableRoomsServlet", urlPatterns = {"/api/rooms/availability"})
public class AvailableRoomsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AvailableRoomsServlet.class.getName());

    private static final String ROOM_AVAILABILITY_SQL =
        "SELECT r.id AS room_id, " +
        "       r.room_type, " +
        "       r.price_per_night, " +
        "       CASE " +
        "           WHEN res.id IS NOT NULL " +
        "                AND CURDATE() >= DATE(res.check_in_date) " +
        "                AND CURDATE() < DATE(res.check_out_date) " +
        "           THEN 'Not Available' " +
        "           ELSE 'Clean & Available' " +
        "       END AS availability_status, " +
        "       res.check_out_date, " +
        "       res.check_in_date, " +
        "       res.guest_name " +
        "FROM rooms r " +
        "LEFT JOIN ( " +
        "    SELECT rv.* FROM reservations rv " +
        "    INNER JOIN ( " +
        "        SELECT room_id, MAX(check_in_date) AS max_checkin " +
        "        FROM reservations " +
        "        GROUP BY room_id " +
        "    ) latest ON rv.room_id = latest.room_id AND rv.check_in_date = latest.max_checkin " +
        ") res ON r.id = res.room_id " +
        "ORDER BY r.id";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!RBACUtil.requireAuth(request, response)) {
            return;
        }

        PrintWriter out = response.getWriter();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            pstmt = conn.prepareStatement(ROOM_AVAILABILITY_SQL);
            rs = pstmt.executeQuery();

            StringBuilder json = new StringBuilder();
            json.append("{\"success\": true, \"rooms\": [");

            boolean first = true;
            int availableCount = 0;
            int totalCount = 0;

            while (rs.next()) {
                if (!first) json.append(",");
                first = false;
                totalCount++;

                int roomId = rs.getInt("room_id");
                String roomType = rs.getString("room_type");
                double pricePerNight = rs.getDouble("price_per_night");
                String status = rs.getString("availability_status");
                String checkOutDate = rs.getString("check_out_date");
                String checkInDate = rs.getString("check_in_date");
                String guestName = rs.getString("guest_name");

                if ("Clean & Available".equals(status)) {
                    availableCount++;
                }

                json.append("{");
                json.append("\"roomId\": ").append(roomId).append(",");
                json.append("\"roomType\": \"").append(escapeJson(roomType)).append("\",");
                json.append("\"pricePerNight\": ").append(pricePerNight).append(",");
                json.append("\"status\": \"").append(escapeJson(status)).append("\",");
                json.append("\"checkOutDate\": ").append(checkOutDate != null ? "\"" + checkOutDate + "\"" : "null").append(",");
                json.append("\"checkInDate\": ").append(checkInDate != null ? "\"" + checkInDate + "\"" : "null").append(",");
                json.append("\"guestName\": ").append(guestName != null ? "\"" + escapeJson(guestName) + "\"" : "null");
                json.append("}");
            }

            json.append("], ");
            json.append("\"totalRooms\": ").append(totalCount).append(", ");
            json.append("\"availableRooms\": ").append(availableCount).append(", ");
            json.append("\"occupiedRooms\": ").append(totalCount - availableCount);
            json.append("}");

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(json.toString());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching room availability", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Database error while fetching room availability\"}");
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database resources", e);
            }
            out.flush();
        }
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
