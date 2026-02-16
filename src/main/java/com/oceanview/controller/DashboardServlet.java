package com.oceanview.controller;

import com.oceanview.factory.ServiceFactory;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;
import com.oceanview.service.ReservationService;
import com.oceanview.service.RoomService;
import com.oceanview.util.RBACUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/api/dashboard"})
public class DashboardServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(DashboardServlet.class.getName());
    private ReservationService reservationService;
    private RoomService roomService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        ServiceFactory factory = ServiceFactory.getInstance();
        reservationService = factory.getReservationService();
        roomService = factory.getRoomService();
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
            List<Room> allRooms = roomService.getAllRooms();
            int totalRooms = allRooms.size();
            int occupiedRooms = 0;
            for (Room r : allRooms) {
                if ("OCCUPIED".equalsIgnoreCase(r.getStatus()) || "RESERVED".equalsIgnoreCase(r.getStatus())) {
                    occupiedRooms++;
                }
            }

            List<Reservation> allReservations = reservationService.getAllReservations();
            int totalReservations = allReservations.size();
            
            LocalDate today = LocalDate.now();
            Date todaySql = Date.valueOf(today);
            int activeReservations = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            
            for (Reservation res : allReservations) {
                if (!res.getCheckInDate().after(todaySql) && !res.getCheckOutDate().before(todaySql)) {
                    activeReservations++;
                }
                if (res.getTotalAmount() != null) {
                    totalRevenue = totalRevenue.add(res.getTotalAmount());
                }
            }
            
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"success\": true,");
            
            // Stats object
            json.append("\"stats\": {");
            json.append("\"totalReservations\": ").append(totalReservations).append(",");
            json.append("\"activeReservations\": ").append(activeReservations).append(",");
            json.append("\"totalRevenue\": ").append(totalRevenue).append(",");
            json.append("\"totalRooms\": ").append(totalRooms).append(",");
            json.append("\"occupiedRooms\": ").append(occupiedRooms);
            json.append("},");
            
            // Recent reservations (last 10)
            json.append("\"recentCheckIns\": [");
            int limit = Math.min(10, allReservations.size());
            for (int i = 0; i < limit; i++) {
                if (i > 0) json.append(",");
                Reservation res = allReservations.get(i);
                // Determine status based on dates
                String status;
                if (res.getCheckOutDate().before(todaySql)) {
                    status = "Checked-Out";
                } else if (!res.getCheckInDate().after(todaySql) && !res.getCheckOutDate().before(todaySql)) {
                    status = "Checked-In";
                } else {
                    status = "Confirmed";
                }
                
                json.append("{");
                json.append("\"reservationId\": ").append(res.getId()).append(",");
                json.append("\"reservationNumber\": \"").append(escapeJson(res.getReservationNumber())).append("\",");
                json.append("\"guestName\": \"").append(escapeJson(res.getGuestName())).append("\",");
                json.append("\"checkInDate\": \"").append(res.getCheckInDate()).append("\",");
                json.append("\"checkOutDate\": \"").append(res.getCheckOutDate()).append("\",");
                json.append("\"roomType\": \"").append(escapeJson(res.getRoomType() != null ? res.getRoomType() : "N/A")).append("\",");
                json.append("\"roomId\": ").append(res.getRoomId()).append(",");
                json.append("\"totalAmount\": ").append(res.getTotalAmount() != null ? res.getTotalAmount() : BigDecimal.ZERO).append(",");
                json.append("\"status\": \"").append(status).append("\"");
                json.append("}");
            }
            json.append("]");
            
            json.append("}");
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(json.toString());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving dashboard data", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error retrieving dashboard data\"}");
        } finally {
            out.flush();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
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
