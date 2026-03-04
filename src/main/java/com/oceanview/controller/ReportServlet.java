package com.oceanview.controller;

import com.oceanview.factory.ServiceFactory;
import com.oceanview.model.Report;
import com.oceanview.service.ReportService;
import com.oceanview.util.RBACUtil;
import jakarta.servlet.ServletException;

import java.time.LocalDate;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "ReportServlet", urlPatterns = {"/api/reports", "/api/reports/*"})
public class ReportServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ReportServlet.class.getName());
    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        super.init();
        reportService = ServiceFactory.getInstance().getReportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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

            if (pathInfo != null && pathInfo.length() > 1) {
                String reportType = pathInfo.substring(1);

                switch (reportType) {
                    case "daily":
                        handleDailyReport(request, response, out);
                        break;
                    case "monthly":
                        handleMonthlyReport(request, response, out);
                        break;
                    case "yearly":
                        handleYearlyReport(request, response, out);
                        break;
                    case "room-type":
                        handleRoomTypeReport(request, response, out);
                        break;
                    case "date-range":
                        handleDateRangeReport(request, response, out);
                        break;
                    default:
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"success\": false, \"message\": \"Invalid report type\"}");
                }
            } else {

                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{");
                out.print("\"success\": true,");
                out.print("\"availableReports\": [");
                out.print("\"daily\", \"monthly\", \"yearly\", \"room-type\", \"date-range\"");
                out.print("]");
                out.print("}");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating report", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error generating report\"}");
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
            String reportType = request.getParameter("reportType");

            if (reportType == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"Report type required\"}");
                return;
            }

            switch (reportType.toLowerCase()) {
                case "daily":
                    handleDailyReport(request, response, out);
                    break;
                case "monthly":
                    handleMonthlyReport(request, response, out);
                    break;
                case "yearly":
                    handleYearlyReport(request, response, out);
                    break;
                case "room-type":
                    handleRoomTypeReport(request, response, out);
                    break;
                case "date-range":
                    handleDateRangeReport(request, response, out);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\": false, \"message\": \"Invalid report type\"}");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating custom report", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error generating custom report\"}");
        } finally {
            out.flush();
        }
    }

    private void handleDailyReport(HttpServletRequest request, HttpServletResponse response,
                                   PrintWriter out) {
        try {
            String dateStr = request.getParameter("date");
            Date date = (dateStr != null) ? Date.valueOf(dateStr) : new Date(System.currentTimeMillis());

            Report report = reportService.generateFinancialReport(date, date);

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(reportToJson(report));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Invalid date format\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating daily report", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error generating daily report\"}");
        }
    }

    private void handleMonthlyReport(HttpServletRequest request, HttpServletResponse response,
                                     PrintWriter out) {
        try {
            String yearStr = request.getParameter("year");
            String monthStr = request.getParameter("month");

            int year = (yearStr != null) ? Integer.parseInt(yearStr) :
                       java.time.LocalDate.now().getYear();
            int month = (monthStr != null) ? Integer.parseInt(monthStr) :
                        java.time.LocalDate.now().getMonthValue();

            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);

            Report report = reportService.generateFinancialReport(
                Date.valueOf(startDate), Date.valueOf(endDate));

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(reportToJson(report));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Invalid year or month\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating monthly report", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error generating monthly report\"}");
        }
    }

    private void handleYearlyReport(HttpServletRequest request, HttpServletResponse response,
                                    PrintWriter out) {
        try {
            String yearStr = request.getParameter("year");
            int year = (yearStr != null) ? Integer.parseInt(yearStr) :
                       java.time.LocalDate.now().getYear();

            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);

            Report report = reportService.generateFinancialReport(
                Date.valueOf(startDate), Date.valueOf(endDate));

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(reportToJson(report));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Invalid year\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating yearly report", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error generating yearly report\"}");
        }
    }

    private void handleRoomTypeReport(HttpServletRequest request, HttpServletResponse response,
                                      PrintWriter out) {
        try {
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");

            if (startDateStr == null || endDateStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"Start date and end date required\"}");
                return;
            }

            List<Report> reports = new java.util.ArrayList<>();

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(reportsToJson(reports));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Invalid date format\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating room type report", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error generating room type report\"}");
        }
    }

    private void handleDateRangeReport(HttpServletRequest request, HttpServletResponse response,
                                       PrintWriter out) {
        try {
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");

            if (startDateStr == null || endDateStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"message\": \"Start date and end date required\"}");
                return;
            }

            Date startDate = Date.valueOf(startDateStr);
            Date endDate = Date.valueOf(endDateStr);

            Report report = reportService.generateFinancialReport(startDate, endDate);

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(reportToJson(report));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"Invalid date format\"}");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating date range report", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"message\": \"Error generating date range report\"}");
        }
    }

    private String reportToJson(Report r) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": true,");
        json.append("\"report\": {");
        json.append("\"reportType\": \"").append(escapeJson(r.getReportType())).append("\",");
        json.append("\"startDate\": \"").append(r.getStartDate()).append("\",");
        json.append("\"endDate\": \"").append(r.getEndDate()).append("\",");
        json.append("\"totalReservations\": ").append(r.getTotalReservations()).append(",");
        json.append("\"totalRevenue\": ").append(r.getTotalRevenue()).append(",");
        json.append("\"averageReservationValue\": ").append(r.getAverageReservationValue()).append(",");
        json.append("\"occupancyRate\": ").append(r.getOccupancyRate()).append(",");
        json.append("\"generatedDate\": \"").append(r.getGeneratedDate()).append("\"");
        json.append("}");
        json.append("}");
        return json.toString();
    }

    private String reportsToJson(List<Report> reports) {
        StringBuilder json = new StringBuilder();
        json.append("{\"success\": true, \"reports\": [");

        for (int i = 0; i < reports.size(); i++) {
            if (i > 0) json.append(",");
            Report r = reports.get(i);
            json.append("{");
            json.append("\"reportType\": \"").append(escapeJson(r.getReportType())).append("\",");
            json.append("\"startDate\": \"").append(r.getStartDate()).append("\",");
            json.append("\"endDate\": \"").append(r.getEndDate()).append("\",");
            json.append("\"totalReservations\": ").append(r.getTotalReservations()).append(",");
            json.append("\"totalRevenue\": ").append(r.getTotalRevenue()).append(",");
            json.append("\"averageReservationValue\": ").append(r.getAverageReservationValue()).append(",");
            json.append("\"occupancyRate\": ").append(r.getOccupancyRate()).append(",");
            json.append("\"generatedDate\": \"").append(r.getGeneratedDate()).append("\"");
            json.append("}");
        }

        json.append("], \"count\": ").append(reports.size()).append("}");
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
