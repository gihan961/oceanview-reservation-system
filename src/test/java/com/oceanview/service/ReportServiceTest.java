package com.oceanview.service;

import com.oceanview.dao.ReportDAO;
import com.oceanview.exception.ValidationException;
import com.oceanview.model.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD Unit Tests for ReportService.
 * Uses Mockito to mock ReportDAO — isolated unit testing of reporting logic.
 * 24 test cases covering financial, occupancy, guest statistics, date validation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Tests")
class ReportServiceTest {

    @Mock
    private ReportDAO mockReportDAO;

    private ReportService reportService;

    private Date startDate;
    private Date endDate;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(mockReportDAO);
        startDate = Date.valueOf(LocalDate.of(2025, 1, 1));
        endDate = Date.valueOf(LocalDate.of(2025, 1, 31));
    }

    // ========== FINANCIAL REPORT TESTS ==========

    @Nested
    @DisplayName("Financial Report Tests")
    class FinancialReportTests {

        @Test
        @DisplayName("TC-RP-01: Generate financial report with valid dates succeeds")
        void testFinancialReportSuccess() throws Exception {
            Report expectedReport = new Report("FINANCIAL", startDate, endDate);
            expectedReport.setTotalRevenue(new BigDecimal("500000.00"));
            expectedReport.setTotalReservations(25);
            when(mockReportDAO.generateFinancialReport(startDate, endDate))
                    .thenReturn(expectedReport);

            Report result = reportService.generateFinancialReport(startDate, endDate);

            assertNotNull(result);
            assertEquals(new BigDecimal("500000.00"), result.getTotalRevenue());
            assertEquals(25, result.getTotalReservations());
        }

        @Test
        @DisplayName("TC-RP-02: Financial report with no data returns empty report")
        void testFinancialReportNoData() throws Exception {
            when(mockReportDAO.generateFinancialReport(startDate, endDate)).thenReturn(null);

            Report result = reportService.generateFinancialReport(startDate, endDate);

            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
            assertEquals(0, result.getTotalReservations());
        }

        @Test
        @DisplayName("TC-RP-03: Financial report with null start date throws ValidationException")
        void testFinancialReportNullStartDate() {
            assertThrows(ValidationException.class,
                    () -> reportService.generateFinancialReport(null, endDate));
        }

        @Test
        @DisplayName("TC-RP-04: Financial report with null end date throws ValidationException")
        void testFinancialReportNullEndDate() {
            assertThrows(ValidationException.class,
                    () -> reportService.generateFinancialReport(startDate, null));
        }

        @Test
        @DisplayName("TC-RP-05: Financial report with end before start throws ValidationException")
        void testFinancialReportEndBeforeStart() {
            assertThrows(ValidationException.class,
                    () -> reportService.generateFinancialReport(endDate, startDate));
        }

        @Test
        @DisplayName("TC-RP-06: Financial report exceeding 5 years throws ValidationException")
        void testFinancialReportExceed5Years() {
            Date farEnd = Date.valueOf(LocalDate.of(2031, 1, 1));
            assertThrows(ValidationException.class,
                    () -> reportService.generateFinancialReport(startDate, farEnd));
        }
    }

    // ========== OCCUPANCY REPORT TESTS ==========

    @Nested
    @DisplayName("Occupancy Report Tests")
    class OccupancyReportTests {

        @Test
        @DisplayName("TC-RP-07: Generate occupancy report returns list")
        void testOccupancyReportSuccess() throws Exception {
            Report r1 = new Report();
            r1.setRoomType("DELUXE");
            r1.setOccupancyRate(75.0);
            Report r2 = new Report();
            r2.setRoomType("SUITE");
            r2.setOccupancyRate(60.0);
            when(mockReportDAO.generateOccupancyReport()).thenReturn(Arrays.asList(r1, r2));

            List<Report> result = reportService.generateOccupancyReport();

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("TC-RP-08: Occupancy report with database error throws ValidationException")
        void testOccupancyReportDatabaseError() throws Exception {
            when(mockReportDAO.generateOccupancyReport())
                    .thenThrow(new RuntimeException("DB error"));

            assertThrows(ValidationException.class,
                    () -> reportService.generateOccupancyReport());
        }
    }

    // ========== GUEST STATISTICS TESTS ==========

    @Nested
    @DisplayName("Guest Statistics Tests")
    class GuestStatisticsTests {

        @Test
        @DisplayName("TC-RP-09: Generate guest statistics with valid dates succeeds")
        void testGuestStatisticsSuccess() throws Exception {
            Report guestReport = new Report();
            guestReport.setGuestName("John Doe");
            guestReport.setTotalReservations(5);
            when(mockReportDAO.generateGuestStatisticsReport(startDate, endDate))
                    .thenReturn(Collections.singletonList(guestReport));

            List<Report> result = reportService.generateGuestStatisticsReport(startDate, endDate);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("TC-RP-10: Guest statistics with null dates throws ValidationException")
        void testGuestStatisticsNullDates() {
            assertThrows(ValidationException.class,
                    () -> reportService.generateGuestStatisticsReport(null, null));
        }
    }

    // ========== REVENUE & RESERVATION COUNT TESTS ==========

    @Nested
    @DisplayName("Revenue & Reservation Count Tests")
    class RevenueTests {

        @Test
        @DisplayName("TC-RP-11: Get total revenue returns correct value")
        void testGetTotalRevenue() throws Exception {
            when(mockReportDAO.getTotalRevenue(startDate, endDate))
                    .thenReturn(new BigDecimal("750000.00"));

            BigDecimal result = reportService.getTotalRevenue(startDate, endDate);

            assertEquals(new BigDecimal("750000.00"), result);
        }

        @Test
        @DisplayName("TC-RP-12: Get total revenue with null returns zero")
        void testGetTotalRevenueNull() throws Exception {
            when(mockReportDAO.getTotalRevenue(startDate, endDate)).thenReturn(null);

            BigDecimal result = reportService.getTotalRevenue(startDate, endDate);

            assertEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("TC-RP-13: Get total reservations returns correct count")
        void testGetTotalReservations() throws Exception {
            when(mockReportDAO.getTotalReservations(startDate, endDate)).thenReturn(42);

            int result = reportService.getTotalReservations(startDate, endDate);

            assertEquals(42, result);
        }

        @Test
        @DisplayName("TC-RP-14: Get reservation statistics with valid dates succeeds")
        void testGetReservationStatistics() throws Exception {
            Report statsReport = new Report("STATISTICS", startDate, endDate);
            statsReport.setTotalReservations(50);
            statsReport.setCompletedReservations(40);
            statsReport.setUpcomingReservations(10);
            when(mockReportDAO.getReservationStatistics(startDate, endDate))
                    .thenReturn(statsReport);

            Report result = reportService.getReservationStatistics(startDate, endDate);

            assertNotNull(result);
            assertEquals(50, result.getTotalReservations());
            assertEquals(40, result.getCompletedReservations());
        }

        @Test
        @DisplayName("TC-RP-15: Get reservation statistics with null returns empty report")
        void testGetReservationStatisticsNull() throws Exception {
            when(mockReportDAO.getReservationStatistics(startDate, endDate)).thenReturn(null);

            Report result = reportService.getReservationStatistics(startDate, endDate);

            assertNotNull(result);
            assertEquals(0, result.getTotalReservations());
        }
    }

    // ========== MONTHLY REPORT TESTS ==========

    @Nested
    @DisplayName("Monthly Report Tests")
    class MonthlyReportTests {

        @Test
        @DisplayName("TC-RP-16: Generate monthly report for valid month succeeds")
        void testMonthlyReportSuccess() throws Exception {
            Report report = new Report("FINANCIAL",
                    Date.valueOf(LocalDate.of(2025, 6, 1)),
                    Date.valueOf(LocalDate.of(2025, 6, 30)));
            report.setTotalRevenue(new BigDecimal("300000"));
            when(mockReportDAO.generateFinancialReport(any(Date.class), any(Date.class)))
                    .thenReturn(report);

            Report result = reportService.generateMonthlyReport(2025, 6);

            assertNotNull(result);
        }

        @Test
        @DisplayName("TC-RP-17: Monthly report with invalid month (0) throws ValidationException")
        void testMonthlyReportInvalidMonth0() {
            assertThrows(ValidationException.class,
                    () -> reportService.generateMonthlyReport(2025, 0));
        }

        @Test
        @DisplayName("TC-RP-18: Monthly report with invalid month (13) throws ValidationException")
        void testMonthlyReportInvalidMonth13() {
            assertThrows(ValidationException.class,
                    () -> reportService.generateMonthlyReport(2025, 13));
        }

        @Test
        @DisplayName("TC-RP-19: Monthly report with invalid year (<2000) throws ValidationException")
        void testMonthlyReportInvalidYearLow() {
            assertThrows(ValidationException.class,
                    () -> reportService.generateMonthlyReport(1999, 6));
        }

        @Test
        @DisplayName("TC-RP-20: Monthly report with invalid year (>2100) throws ValidationException")
        void testMonthlyReportInvalidYearHigh() {
            assertThrows(ValidationException.class,
                    () -> reportService.generateMonthlyReport(2101, 6));
        }
    }

    // ========== YEARLY REPORT TESTS ==========

    @Nested
    @DisplayName("Yearly Report Tests")
    class YearlyReportTests {

        @Test
        @DisplayName("TC-RP-21: Generate yearly report for valid year succeeds")
        void testYearlyReportSuccess() throws Exception {
            Report report = new Report("FINANCIAL",
                    Date.valueOf(LocalDate.of(2025, 1, 1)),
                    Date.valueOf(LocalDate.of(2025, 12, 31)));
            when(mockReportDAO.generateFinancialReport(any(Date.class), any(Date.class)))
                    .thenReturn(report);

            Report result = reportService.generateYearlyReport(2025);

            assertNotNull(result);
        }

        @Test
        @DisplayName("TC-RP-22: Yearly report with invalid year throws ValidationException")
        void testYearlyReportInvalidYear() {
            assertThrows(ValidationException.class,
                    () -> reportService.generateYearlyReport(1999));
        }
    }

    // ========== AVERAGE OCCUPANCY RATE TESTS ==========

    @Nested
    @DisplayName("Average Occupancy Rate Tests")
    class OccupancyRateTests {

        @Test
        @DisplayName("TC-RP-23: Calculate average occupancy rate returns correct average")
        void testAverageOccupancyRate() throws Exception {
            Report r1 = new Report();
            r1.setOccupancyRate(80.0);
            Report r2 = new Report();
            r2.setOccupancyRate(60.0);
            when(mockReportDAO.generateOccupancyReport()).thenReturn(Arrays.asList(r1, r2));

            double result = reportService.calculateAverageOccupancyRate();

            assertEquals(70.0, result, 0.01);
        }

        @Test
        @DisplayName("TC-RP-24: Average occupancy rate with no data returns 0.0")
        void testAverageOccupancyRateEmpty() throws Exception {
            when(mockReportDAO.generateOccupancyReport()).thenReturn(Collections.emptyList());

            double result = reportService.calculateAverageOccupancyRate();

            assertEquals(0.0, result, 0.01);
        }
    }
}
