package com.oceanview.controller;

import com.oceanview.exception.ReservationException;
import com.oceanview.exception.ValidationException;
import com.oceanview.factory.ServiceFactory;
import com.oceanview.model.Reservation;
import com.oceanview.service.ReservationService;
import com.oceanview.util.LoggerUtil;
import com.oceanview.util.RBACUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "ReservationServlet", urlPatterns = {"/api/reservations", "/api/reservations/*"})
public class ReservationServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerUtil.getLogger(ReservationServlet.class);
    private ReservationService reservationService;

    @Override
    public void init() throws ServletException {
        super.init();
        reservationService = ServiceFactory.getInstance().getReservationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (!RBACUtil.requireAuth(request, response)) return;

        PrintWriter out = response.getWriter();
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.length() > 1) {
                int reservationId = Integer.parseInt(pathInfo.substring(1));
                Reservation reservation = reservationService.getReservationById(reservationId);
                if (reservation != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(reservationToJson(reservation));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"success\":false,\"message\":\"Reservation not found\"}");
                }
            } else {
                String date = request.getParameter("date");
                List<Reservation> reservations = "today".equals(date)
                        ? reservationService.getTodayCheckIns()
                        : reservationService.getAllReservations();
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(reservationsToJson(reservations));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Invalid reservation ID\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservations", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Error retrieving reservations\"}");
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
                RBACUtil.ROLE_ADMIN, RBACUtil.ROLE_MANAGER, RBACUtil.ROLE_STAFF)) return;

        PrintWriter out = response.getWriter();
        String guestName = null;
        try {
            StringBuilder jsonBuffer = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) jsonBuffer.append(line);
            String jsonString = jsonBuffer.toString();

            guestName = extractJsonValue(jsonString, "guestName");
            String address = extractJsonValue(jsonString, "address");
            String contactNumber = extractJsonValue(jsonString, "contactNumber");
            int roomId = Integer.parseInt(extractJsonValue(jsonString, "roomId"));
            Date checkInDate = Date.valueOf(extractJsonValue(jsonString, "checkInDate"));
            Date checkOutDate = Date.valueOf(extractJsonValue(jsonString, "checkOutDate"));

            Reservation reservation = reservationService.createReservation(
                    guestName, address, contactNumber, roomId, checkInDate, checkOutDate);
            LoggerUtil.logReservationCreation(reservation.getId(), guestName, roomId, getUsername(request));

            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print("{\"success\":true,\"message\":\"Reservation created successfully\",");
            out.print("\"reservationNumber\":\"" + escapeJson(reservation.getReservationNumber()) + "\",");
            out.print("\"reservation\":" + reservationToJson(reservation) + "}");
        } catch (ValidationException e) {
            LoggerUtil.logValidationError("Reservation", guestName != null ? guestName : "unknown", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (ReservationException e) {
            LoggerUtil.logApplicationError("ReservationServlet", "createReservation", e);
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print("{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Invalid input parameters\"}");
        } catch (Exception e) {
            LoggerUtil.logApplicationError("ReservationServlet", "createReservation", e);
            LOGGER.log(Level.SEVERE, "Error creating reservation", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Error creating reservation\"}");
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
                RBACUtil.ROLE_ADMIN, RBACUtil.ROLE_MANAGER)) return;

        PrintWriter out = response.getWriter();
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Reservation ID required\"}");
                return;
            }
            int reservationId = Integer.parseInt(pathInfo.substring(1));
            Reservation reservation = reservationService.getReservationById(reservationId);
            if (reservation == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\":false,\"message\":\"Reservation not found\"}");
                return;
            }

            String guestName = request.getParameter("guestName");
            if (guestName != null) reservation.setGuestName(guestName);
            String address = request.getParameter("address");
            if (address != null) reservation.setAddress(address);
            String contactNumber = request.getParameter("contactNumber");
            if (contactNumber != null) reservation.setContactNumber(contactNumber);
            String checkInStr = request.getParameter("checkInDate");
            if (checkInStr != null) reservation.setCheckInDate(Date.valueOf(checkInStr));
            String checkOutStr = request.getParameter("checkOutDate");
            if (checkOutStr != null) reservation.setCheckOutDate(Date.valueOf(checkOutStr));

            boolean updated = reservationService.updateReservation(reservation);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"success\":true,\"message\":\"Reservation updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"message\":\"Failed to update reservation\"}");
            }
        } catch (ValidationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating reservation", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Error updating reservation\"}");
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
                RBACUtil.ROLE_ADMIN, RBACUtil.ROLE_MANAGER)) return;

        PrintWriter out = response.getWriter();
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Reservation ID required\"}");
                return;
            }
            int reservationId = Integer.parseInt(pathInfo.substring(1));
            boolean deleted = reservationService.cancelReservation(reservationId);
            if (deleted) {
                LoggerUtil.logReservationDeletion(reservationId, getUsername(request));
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"success\":true,\"message\":\"Reservation cancelled successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\":false,\"message\":\"Reservation not found\"}");
            }
        } catch (Exception e) {
            LoggerUtil.logApplicationError("ReservationServlet", "cancelReservation", e);
            LOGGER.log(Level.SEVERE, "Error cancelling reservation", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Error cancelling reservation\"}");
        } finally {
            out.flush();
        }
    }

    private String reservationToJson(Reservation r) {
        return "{\"id\":" + r.getId()
                + ",\"reservationNumber\":\"" + escapeJson(r.getReservationNumber()) + "\""
                + ",\"guestName\":\"" + escapeJson(r.getGuestName()) + "\""
                + ",\"address\":\"" + escapeJson(r.getAddress()) + "\""
                + ",\"contactNumber\":\"" + escapeJson(r.getContactNumber()) + "\""
                + ",\"roomId\":" + r.getRoomId()
                + ",\"checkInDate\":\"" + r.getCheckInDate() + "\""
                + ",\"checkOutDate\":\"" + r.getCheckOutDate() + "\""
                + ",\"totalAmount\":" + r.getTotalAmount()
                + ",\"createdAt\":\"" + r.getCreatedAt() + "\"}";
    }

    private String reservationsToJson(List<Reservation> reservations) {
        StringBuilder json = new StringBuilder("{\"success\":true,\"reservations\":[");
        for (int i = 0; i < reservations.size(); i++) {
            if (i > 0) json.append(",");
            json.append(reservationToJson(reservations.get(i)));
        }
        json.append("],\"count\":").append(reservations.size()).append("}");
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String getUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null && session.getAttribute("username") != null)
                ? (String) session.getAttribute("username") : "anonymous";
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int idx = json.indexOf(searchKey);
        if (idx == -1) return null;
        idx += searchKey.length();
        while (idx < json.length() && Character.isWhitespace(json.charAt(idx))) idx++;
        if (json.charAt(idx) == '"') {
            idx++;
            return json.substring(idx, json.indexOf('"', idx));
        }
        int end = idx;
        while (end < json.length() && !Character.isWhitespace(json.charAt(end))
                && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        return json.substring(idx, end);
    }
}
