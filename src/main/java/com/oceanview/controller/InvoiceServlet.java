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
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "InvoiceServlet", urlPatterns = {"/api/invoice", "/api/invoice/*"})
public class InvoiceServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(InvoiceServlet.class.getName());
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
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"Reservation ID required\"}");
                return;
            }

            int reservationId = Integer.parseInt(pathInfo.substring(1));

            Reservation reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\": false, \"message\": \"Reservation not found\"}");
                return;
            }

            Room room = roomService.getRoomById(reservation.getRoomId());

            if (room == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\": false, \"message\": \"Room details not found\"}");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(generateInvoiceJson(reservation, room));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Invalid reservation ID\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating invoice", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error generating invoice\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!RBACUtil.requireAuth(request, response)) {
            return;
        }

        PrintWriter out = response.getWriter();

        try {
            String reservationIdStr = request.getParameter("reservationId");

            if (reservationIdStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"Reservation ID required\"}");
                return;
            }

            int reservationId = Integer.parseInt(reservationIdStr);

            Reservation reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\": false, \"message\": \"Reservation not found\"}");
                return;
            }

            Room room = roomService.getRoomById(reservation.getRoomId());

            if (room == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\": false, \"message\": \"Room details not found\"}");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(generateInvoiceJson(reservation, room));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Invalid reservation ID\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating invoice", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error generating invoice\"}");
        } finally {
            out.flush();
        }
    }

    private String generateInvoiceJson(Reservation reservation, Room room) {

        long nights = ChronoUnit.DAYS.between(
            reservation.getCheckInDate().toLocalDate(),
            reservation.getCheckOutDate().toLocalDate()
        );

        BigDecimal pricePerNight = room.getPricePerNight();
        BigDecimal subtotal = pricePerNight.multiply(BigDecimal.valueOf(nights));

        BigDecimal taxRate = new BigDecimal("0.10");
        BigDecimal tax = subtotal.multiply(taxRate);

        BigDecimal serviceChargeRate = new BigDecimal("0.05");
        BigDecimal serviceCharge = subtotal.multiply(serviceChargeRate);

        BigDecimal total = subtotal.add(tax).add(serviceCharge);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": true,");
        json.append("\"invoice\": {");

        json.append("\"hotelName\": \"OceanView Hotel\",");
        json.append("\"hotelAddress\": \"123 Beach Road, Colombo, Sri Lanka\",");
        json.append("\"hotelPhone\": \"+94 11 234 5678\",");
        json.append("\"hotelEmail\": \"info@oceanview.com\",");

        json.append("\"reservationNumber\": \"").append(escapeJson(reservation.getReservationNumber())).append("\",");
        json.append("\"invoiceDate\": \"").append(java.time.LocalDate.now()).append("\",");

        json.append("\"guestName\": \"").append(escapeJson(reservation.getGuestName())).append("\",");
        json.append("\"guestAddress\": \"").append(escapeJson(reservation.getAddress())).append("\",");
        json.append("\"guestContact\": \"").append(escapeJson(reservation.getContactNumber())).append("\",");

        json.append("\"roomId\": ").append(room.getId()).append(",");
        json.append("\"roomType\": \"").append(escapeJson(room.getRoomType())).append("\",");
        json.append("\"pricePerNight\": ").append(pricePerNight).append(",");

        json.append("\"checkInDate\": \"").append(reservation.getCheckInDate()).append("\",");
        json.append("\"checkOutDate\": \"").append(reservation.getCheckOutDate()).append("\",");
        json.append("\"numberOfNights\": ").append(nights).append(",");

        json.append("\"charges\": {");
        json.append("\"roomCharges\": ").append(subtotal).append(",");
        json.append("\"tax\": ").append(tax).append(",");
        json.append("\"taxRate\": ").append(taxRate.multiply(new BigDecimal("100"))).append(",");
        json.append("\"serviceCharge\": ").append(serviceCharge).append(",");
        json.append("\"serviceChargeRate\": ").append(serviceChargeRate.multiply(new BigDecimal("100"))).append(",");
        json.append("\"totalAmount\": ").append(total);
        json.append("},");

        json.append("\"paymentStatus\": \"Paid\",");
        json.append("\"paymentMethod\": \"Cash\",");

        json.append("\"footer\": \"Thank you for choosing OceanView Hotel. We hope you enjoyed your stay!\"");

        json.append("}");
        json.append("}");

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
