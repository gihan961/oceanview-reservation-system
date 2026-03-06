package com.oceanview.service;

import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.RoomDAO;
import com.oceanview.exception.ReservationException;
import com.oceanview.exception.ValidationException;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD Unit Tests for ReservationService.
 * Uses Mockito to mock ReservationDAO & RoomDAO — isolated unit testing.
 * 28 test cases covering reservation CRUD, date validation, billing, availability.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService Tests")
class ReservationServiceTest {

    @Mock
    private ReservationDAO mockReservationDAO;

    @Mock
    private RoomDAO mockRoomDAO;

    private ReservationService reservationService;

    // Test dates — future dates to avoid "past date" validation errors
    private Date checkInDate;
    private Date checkOutDate;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(mockReservationDAO, mockRoomDAO);
        // Use dates in the near future
        LocalDate futureCheckIn = LocalDate.now().plusDays(1);
        LocalDate futureCheckOut = LocalDate.now().plusDays(4);
        checkInDate = Date.valueOf(futureCheckIn);
        checkOutDate = Date.valueOf(futureCheckOut);
    }

    // ========== CREATE RESERVATION TESTS ==========

    @Nested
    @DisplayName("Create Reservation Tests")
    class CreateReservationTests {

        @Test
        @DisplayName("TC-RV-01: Create reservation with valid data succeeds")
        void testCreateReservationSuccess() throws Exception {
            Room room = new Room("DELUXE", new BigDecimal("15000.00"), "AVAILABLE");
            room.setId(1);
            when(mockRoomDAO.findById(1)).thenReturn(room);
            when(mockReservationDAO.findByRoomId(1)).thenReturn(Collections.emptyList());
            when(mockReservationDAO.create(any(Reservation.class))).thenReturn(true);
            when(mockReservationDAO.findByReservationNumber(anyString())).thenReturn(null);
            when(mockRoomDAO.updateStatus(eq(1), eq("RESERVED"))).thenReturn(true);

            Reservation result = reservationService.createReservation(
                    "John Doe", "123 Main St", "0771234567",
                    1, checkInDate, checkOutDate);

            assertNotNull(result);
            assertEquals("John Doe", result.getGuestName());
            assertEquals(1, result.getRoomId());
            assertNotNull(result.getReservationNumber());
            assertTrue(result.getReservationNumber().startsWith("RES"));
            verify(mockReservationDAO).create(any(Reservation.class));
            verify(mockRoomDAO).updateStatus(1, "RESERVED");
        }

        @Test
        @DisplayName("TC-RV-02: Create reservation for unavailable room throws ReservationException")
        void testCreateReservationRoomUnavailable() throws Exception {
            when(mockRoomDAO.findById(1)).thenReturn(null); // room not found = unavailable

            assertThrows(ReservationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "123 Main St", "0771234567",
                            1, checkInDate, checkOutDate));
        }

        @Test
        @DisplayName("TC-RV-03: Create reservation for maintenance room throws ReservationException")
        void testCreateReservationRoomInMaintenance() throws Exception {
            Room room = new Room("DELUXE", new BigDecimal("15000"), "MAINTENANCE");
            room.setId(1);
            when(mockRoomDAO.findById(1)).thenReturn(room);

            assertThrows(ReservationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "123 Main St", "0771234567",
                            1, checkInDate, checkOutDate));
        }

        @Test
        @DisplayName("TC-RV-04: Create reservation with empty guest name throws ValidationException")
        void testCreateReservationEmptyGuest() {
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "", "123 Main St", "0771234567",
                            1, checkInDate, checkOutDate));
        }

        @Test
        @DisplayName("TC-RV-05: Create reservation with short guest name (<2 chars) throws ValidationException")
        void testCreateReservationShortGuestName() {
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "J", "123 Main St", "0771234567",
                            1, checkInDate, checkOutDate));
        }

        @Test
        @DisplayName("TC-RV-06: Create reservation with empty address throws ValidationException")
        void testCreateReservationEmptyAddress() {
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "", "0771234567",
                            1, checkInDate, checkOutDate));
        }

        @Test
        @DisplayName("TC-RV-07: Create reservation with empty contact throws ValidationException")
        void testCreateReservationEmptyContact() {
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "123 Main St", "",
                            1, checkInDate, checkOutDate));
        }

        @Test
        @DisplayName("TC-RV-08: Create reservation with short contact (<7 digits) throws ValidationException")
        void testCreateReservationShortContact() {
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "123 Main St", "12345",
                            1, checkInDate, checkOutDate));
        }

        @Test
        @DisplayName("TC-RV-09: Create reservation with invalid room ID throws ValidationException")
        void testCreateReservationInvalidRoomId() {
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "123 Main St", "0771234567",
                            0, checkInDate, checkOutDate));
        }
    }

    // ========== DATE VALIDATION TESTS ==========

    @Nested
    @DisplayName("Date Validation Tests")
    class DateValidationTests {

        @Test
        @DisplayName("TC-RV-10: Check-in date in the past throws ValidationException")
        void testCheckInDateInPast() {
            Date pastDate = Date.valueOf(LocalDate.now().minusDays(1));
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "123 Main St", "0771234567",
                            1, pastDate, checkOutDate));
        }

        @Test
        @DisplayName("TC-RV-11: Check-out before check-in throws ValidationException")
        void testCheckOutBeforeCheckIn() {
            Date futureDate = Date.valueOf(LocalDate.now().plusDays(5));
            Date earlierDate = Date.valueOf(LocalDate.now().plusDays(2));
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "123 Main St", "0771234567",
                            1, futureDate, earlierDate));
        }

        @Test
        @DisplayName("TC-RV-12: Reservation exceeding 30 nights throws ValidationException")
        void testExceed30Nights() {
            Date longCheckIn = Date.valueOf(LocalDate.now().plusDays(1));
            Date longCheckOut = Date.valueOf(LocalDate.now().plusDays(32));
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "123 Main St", "0771234567",
                            1, longCheckIn, longCheckOut));
        }

        @Test
        @DisplayName("TC-RV-13: Reservation more than 365 days in advance throws ValidationException")
        void testExceed365DaysAdvance() {
            Date farCheckIn = Date.valueOf(LocalDate.now().plusDays(366));
            Date farCheckOut = Date.valueOf(LocalDate.now().plusDays(368));
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "123 Main St", "0771234567",
                            1, farCheckIn, farCheckOut));
        }

        @Test
        @DisplayName("TC-RV-14: Null check-in date throws ValidationException")
        void testNullCheckInDate() {
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "123 Main St", "0771234567",
                            1, null, checkOutDate));
        }

        @Test
        @DisplayName("TC-RV-15: Null check-out date throws ValidationException")
        void testNullCheckOutDate() {
            assertThrows(ValidationException.class,
                    () -> reservationService.createReservation(
                            "John Doe", "123 Main St", "0771234567",
                            1, checkInDate, null));
        }
    }

    // ========== CANCEL RESERVATION TESTS ==========

    @Nested
    @DisplayName("Cancel Reservation Tests")
    class CancelReservationTests {

        @Test
        @DisplayName("TC-RV-16: Cancel valid reservation succeeds and resets room status")
        void testCancelReservationSuccess() throws Exception {
            Reservation reservation = new Reservation();
            reservation.setId(1);
            reservation.setReservationNumber("RES20250101001");
            reservation.setRoomId(5);
            when(mockReservationDAO.findById(1)).thenReturn(reservation);
            when(mockReservationDAO.delete(1)).thenReturn(true);
            when(mockRoomDAO.updateStatus(5, Room.STATUS_AVAILABLE)).thenReturn(true);

            boolean result = reservationService.cancelReservation(1);

            assertTrue(result);
            verify(mockReservationDAO).delete(1);
            verify(mockRoomDAO).updateStatus(5, Room.STATUS_AVAILABLE);
        }

        @Test
        @DisplayName("TC-RV-17: Cancel with invalid ID throws ValidationException")
        void testCancelReservationInvalidId() {
            assertThrows(ValidationException.class,
                    () -> reservationService.cancelReservation(0));
        }

        @Test
        @DisplayName("TC-RV-18: Cancel non-existent reservation throws ValidationException")
        void testCancelReservationNotFound() throws Exception {
            when(mockReservationDAO.findById(999)).thenReturn(null);

            assertThrows(ValidationException.class,
                    () -> reservationService.cancelReservation(999));
        }
    }

    // ========== CALCULATE TOTAL BILL TESTS ==========

    @Nested
    @DisplayName("Calculate Total Bill Tests")
    class CalculateBillTests {

        @Test
        @DisplayName("TC-RV-19: Calculate bill for 3 nights returns correct total")
        void testCalculateBill3Nights() throws Exception {
            BigDecimal price = new BigDecimal("15000.00");
            Date in = Date.valueOf(LocalDate.now().plusDays(1));
            Date out = Date.valueOf(LocalDate.now().plusDays(4));

            BigDecimal total = reservationService.calculateTotalBill(price, in, out);

            assertEquals(new BigDecimal("45000.00"), total);
        }

        @Test
        @DisplayName("TC-RV-20: Calculate bill for 1 night returns price per night")
        void testCalculateBill1Night() throws Exception {
            BigDecimal price = new BigDecimal("10000.00");
            Date in = Date.valueOf(LocalDate.now().plusDays(1));
            Date out = Date.valueOf(LocalDate.now().plusDays(2));

            BigDecimal total = reservationService.calculateTotalBill(price, in, out);

            assertEquals(new BigDecimal("10000.00"), total);
        }

        @Test
        @DisplayName("TC-RV-21: Calculate bill for 30 nights (max) returns correct total")
        void testCalculateBill30Nights() throws Exception {
            BigDecimal price = new BigDecimal("5000.00");
            Date in = Date.valueOf(LocalDate.now().plusDays(1));
            Date out = Date.valueOf(LocalDate.now().plusDays(31));

            BigDecimal total = reservationService.calculateTotalBill(price, in, out);

            assertEquals(new BigDecimal("150000.00"), total);
        }

        @Test
        @DisplayName("TC-RV-22: Calculate bill with null price throws ValidationException")
        void testCalculateBillNullPrice() {
            assertThrows(ValidationException.class,
                    () -> reservationService.calculateTotalBill(null, checkInDate, checkOutDate));
        }

        @Test
        @DisplayName("TC-RV-23: Calculate bill with zero price throws ValidationException")
        void testCalculateBillZeroPrice() {
            assertThrows(ValidationException.class,
                    () -> reservationService.calculateTotalBill(BigDecimal.ZERO, checkInDate, checkOutDate));
        }
    }

    // ========== SEARCH & RETRIEVAL TESTS ==========

    @Nested
    @DisplayName("Search & Retrieval Tests")
    class SearchTests {

        @Test
        @DisplayName("TC-RV-24: Get reservation by valid ID returns reservation")
        void testGetReservationById() throws Exception {
            Reservation reservation = new Reservation();
            reservation.setId(1);
            reservation.setGuestName("John Doe");
            when(mockReservationDAO.findById(1)).thenReturn(reservation);

            Reservation result = reservationService.getReservationById(1);

            assertNotNull(result);
            assertEquals("John Doe", result.getGuestName());
        }

        @Test
        @DisplayName("TC-RV-25: Get reservation by invalid ID throws ValidationException")
        void testGetReservationByInvalidId() {
            assertThrows(ValidationException.class,
                    () -> reservationService.getReservationById(0));
        }

        @Test
        @DisplayName("TC-RV-26: Search by empty guest name throws ValidationException")
        void testSearchByEmptyGuestName() {
            assertThrows(ValidationException.class,
                    () -> reservationService.searchReservationsByGuest(""));
        }

        @Test
        @DisplayName("TC-RV-27: Get reservation by empty number throws ValidationException")
        void testGetByEmptyNumber() {
            assertThrows(ValidationException.class,
                    () -> reservationService.getReservationByNumber(""));
        }

        @Test
        @DisplayName("TC-RV-28: Get all reservations returns list")
        void testGetAllReservations() throws Exception {
            List<Reservation> reservations = Arrays.asList(
                    new Reservation(), new Reservation()
            );
            when(mockReservationDAO.findAll()).thenReturn(reservations);

            List<Reservation> result = reservationService.getAllReservations();

            assertNotNull(result);
            assertEquals(2, result.size());
        }
    }

    // ========== GENERATE RESERVATION NUMBER TESTS ==========

    @Nested
    @DisplayName("Reservation Number Generation Tests")
    class ReservationNumberTests {

        @Test
        @DisplayName("TC-RV-29: Generated reservation number starts with RES")
        void testReservationNumberFormat() throws Exception {
            when(mockReservationDAO.findByReservationNumber(anyString())).thenReturn(null);

            String number = reservationService.generateReservationNumber();

            assertNotNull(number);
            assertTrue(number.startsWith("RES"));
            assertEquals(14, number.length()); // RES + 8 date digits + 3 counter digits
        }

        @Test
        @DisplayName("TC-RV-30: Two consecutive reservation numbers are different")
        void testReservationNumberUniqueness() throws Exception {
            when(mockReservationDAO.findByReservationNumber(anyString())).thenReturn(null);

            String number1 = reservationService.generateReservationNumber();
            String number2 = reservationService.generateReservationNumber();

            assertNotEquals(number1, number2);
        }
    }

    // ========== UPDATE RESERVATION TESTS ==========

    @Nested
    @DisplayName("Update Reservation Tests")
    class UpdateReservationTests {

        @Test
        @DisplayName("TC-RV-31: Update null reservation throws ValidationException")
        void testUpdateNullReservation() {
            assertThrows(ValidationException.class,
                    () -> reservationService.updateReservation(null));
        }
    }

    // ========== TODAY'S CHECK-IN/OUT TESTS ==========

    @Nested
    @DisplayName("Today Check-In/Out Tests")
    class TodayTests {

        @Test
        @DisplayName("TC-RV-32: Get today's check-ins returns list")
        void testGetTodayCheckIns() throws Exception {
            when(mockReservationDAO.findCheckInsToday()).thenReturn(
                    Collections.singletonList(new Reservation()));

            List<Reservation> result = reservationService.getTodayCheckIns();

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("TC-RV-33: Get today's check-outs returns list")
        void testGetTodayCheckOuts() throws Exception {
            when(mockReservationDAO.findCheckOutsToday()).thenReturn(
                    Collections.emptyList());

            List<Reservation> result = reservationService.getTodayCheckOuts();

            assertNotNull(result);
            assertEquals(0, result.size());
        }
    }
}
