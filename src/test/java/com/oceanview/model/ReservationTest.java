package com.oceanview.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Unit Tests for Reservation Model
 * Tests constructors, getters/setters, date calculations, equals, hashCode
 */
@DisplayName("Reservation Model Tests")
class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates empty Reservation")
        void testDefaultConstructor() {
            Reservation defaultRes = new Reservation();
            assertNotNull(defaultRes);
            assertEquals(0, defaultRes.getId());
            assertNull(defaultRes.getReservationNumber());
            assertNull(defaultRes.getGuestName());
        }

        @Test
        @DisplayName("8-arg constructor sets all booking fields")
        void testEightArgConstructor() {
            Date checkIn = Date.valueOf(LocalDate.now().plusDays(1));
            Date checkOut = Date.valueOf(LocalDate.now().plusDays(3));
            BigDecimal total = new BigDecimal("45000.00");

            Reservation res = new Reservation("RES20260305001", "John Smith",
                    "123 Main St", "0771234567", 1, checkIn, checkOut, total);

            assertEquals("RES20260305001", res.getReservationNumber());
            assertEquals("John Smith", res.getGuestName());
            assertEquals("123 Main St", res.getAddress());
            assertEquals("0771234567", res.getContactNumber());
            assertEquals(1, res.getRoomId());
            assertEquals(checkIn, res.getCheckInDate());
            assertEquals(checkOut, res.getCheckOutDate());
            assertEquals(total, res.getTotalAmount());
        }

        @Test
        @DisplayName("10-arg constructor sets all fields including id and createdAt")
        void testTenArgConstructor() {
            Date checkIn = Date.valueOf(LocalDate.now().plusDays(1));
            Date checkOut = Date.valueOf(LocalDate.now().plusDays(3));
            BigDecimal total = new BigDecimal("45000.00");
            Timestamp now = new Timestamp(System.currentTimeMillis());

            Reservation res = new Reservation(1, "RES20260305001", "John Smith",
                    "123 Main St", "0771234567", 1, checkIn, checkOut, total, now);

            assertEquals(1, res.getId());
            assertEquals("RES20260305001", res.getReservationNumber());
            assertEquals(now, res.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Set and get reservation ID")
        void testSetGetId() {
            reservation.setId(100);
            assertEquals(100, reservation.getId());
        }

        @Test
        @DisplayName("Set and get reservation number")
        void testSetGetReservationNumber() {
            reservation.setReservationNumber("RES20260305001");
            assertEquals("RES20260305001", reservation.getReservationNumber());
        }

        @Test
        @DisplayName("Set and get guest name")
        void testSetGetGuestName() {
            reservation.setGuestName("Jane Doe");
            assertEquals("Jane Doe", reservation.getGuestName());
        }

        @Test
        @DisplayName("Set and get address")
        void testSetGetAddress() {
            reservation.setAddress("456 Ocean Drive");
            assertEquals("456 Ocean Drive", reservation.getAddress());
        }

        @Test
        @DisplayName("Set and get contact number")
        void testSetGetContactNumber() {
            reservation.setContactNumber("0771234567");
            assertEquals("0771234567", reservation.getContactNumber());
        }

        @Test
        @DisplayName("Set and get room ID")
        void testSetGetRoomId() {
            reservation.setRoomId(5);
            assertEquals(5, reservation.getRoomId());
        }

        @Test
        @DisplayName("Set and get room type (joined field)")
        void testSetGetRoomType() {
            reservation.setRoomType("DELUXE");
            assertEquals("DELUXE", reservation.getRoomType());
        }

        @Test
        @DisplayName("Set and get price per night (joined field)")
        void testSetGetPricePerNight() {
            BigDecimal price = new BigDecimal("35000.00");
            reservation.setPricePerNight(price);
            assertEquals(price, reservation.getPricePerNight());
        }

        @Test
        @DisplayName("Set and get total amount")
        void testSetGetTotalAmount() {
            BigDecimal total = new BigDecimal("105000.00");
            reservation.setTotalAmount(total);
            assertEquals(total, reservation.getTotalAmount());
        }
    }

    @Nested
    @DisplayName("Date Calculation Tests")
    class DateCalculationTests {

        @Test
        @DisplayName("getNumberOfNights() returns correct number of nights")
        void testGetNumberOfNights_ThreeNights() {
            reservation.setCheckInDate(Date.valueOf(LocalDate.of(2026, 3, 10)));
            reservation.setCheckOutDate(Date.valueOf(LocalDate.of(2026, 3, 13)));
            assertEquals(3, reservation.getNumberOfNights());
        }

        @Test
        @DisplayName("getNumberOfNights() returns 1 for single night stay")
        void testGetNumberOfNights_OneNight() {
            reservation.setCheckInDate(Date.valueOf(LocalDate.of(2026, 3, 10)));
            reservation.setCheckOutDate(Date.valueOf(LocalDate.of(2026, 3, 11)));
            assertEquals(1, reservation.getNumberOfNights());
        }

        @Test
        @DisplayName("getNumberOfNights() returns 7 for week-long stay")
        void testGetNumberOfNights_SevenNights() {
            reservation.setCheckInDate(Date.valueOf(LocalDate.of(2026, 3, 1)));
            reservation.setCheckOutDate(Date.valueOf(LocalDate.of(2026, 3, 8)));
            assertEquals(7, reservation.getNumberOfNights());
        }

        @Test
        @DisplayName("getNumberOfNights() returns 0 when dates are null")
        void testGetNumberOfNights_NullDates() {
            assertEquals(0, reservation.getNumberOfNights());
        }

        @Test
        @DisplayName("getNumberOfNights() handles month boundary correctly")
        void testGetNumberOfNights_MonthBoundary() {
            reservation.setCheckInDate(Date.valueOf(LocalDate.of(2026, 3, 30)));
            reservation.setCheckOutDate(Date.valueOf(LocalDate.of(2026, 4, 2)));
            assertEquals(3, reservation.getNumberOfNights());
        }
    }

    @Nested
    @DisplayName("Equals & HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals() returns true for same id and reservationNumber")
        void testEquals_Same() {
            Date checkIn = Date.valueOf(LocalDate.now().plusDays(1));
            Date checkOut = Date.valueOf(LocalDate.now().plusDays(3));
            BigDecimal total = new BigDecimal("45000.00");

            Reservation res1 = new Reservation(1, "RES001", "John", "Addr",
                    "077", 1, checkIn, checkOut, total, null);
            Reservation res2 = new Reservation(1, "RES001", "Jane", "OtherAddr",
                    "078", 2, checkIn, checkOut, total, null);
            assertEquals(res1, res2);
        }

        @Test
        @DisplayName("equals() returns false for different id")
        void testEquals_DifferentId() {
            Reservation res1 = new Reservation();
            res1.setId(1);
            res1.setReservationNumber("RES001");

            Reservation res2 = new Reservation();
            res2.setId(2);
            res2.setReservationNumber("RES001");

            assertNotEquals(res1, res2);
        }

        @Test
        @DisplayName("equals() returns false for null")
        void testEquals_Null() {
            Reservation res1 = new Reservation();
            res1.setId(1);
            assertNotEquals(res1, null);
        }
    }

    @Test
    @DisplayName("toString() contains reservation details")
    void testToString() {
        reservation.setId(1);
        reservation.setReservationNumber("RES20260305001");
        reservation.setGuestName("John Smith");
        reservation.setCheckInDate(Date.valueOf(LocalDate.of(2026, 3, 10)));
        reservation.setCheckOutDate(Date.valueOf(LocalDate.of(2026, 3, 13)));

        String result = reservation.toString();
        assertTrue(result.contains("RES20260305001"));
        assertTrue(result.contains("John Smith"));
        assertTrue(result.contains("numberOfNights=3"));
    }
}
