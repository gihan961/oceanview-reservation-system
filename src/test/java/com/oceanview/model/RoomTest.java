package com.oceanview.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Unit Tests for Room Model
 * Tests constructors, getters/setters, status checks, constants, equals, hashCode
 */
@DisplayName("Room Model Tests")
class RoomTest {

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates empty Room")
        void testDefaultConstructor() {
            Room defaultRoom = new Room();
            assertNotNull(defaultRoom);
            assertEquals(0, defaultRoom.getId());
            assertNull(defaultRoom.getRoomType());
            assertNull(defaultRoom.getPricePerNight());
            assertNull(defaultRoom.getStatus());
        }

        @Test
        @DisplayName("3-arg constructor sets type, price, status")
        void testThreeArgConstructor() {
            Room paramRoom = new Room("SINGLE", new BigDecimal("15000.00"), "AVAILABLE");
            assertEquals("SINGLE", paramRoom.getRoomType());
            assertEquals(new BigDecimal("15000.00"), paramRoom.getPricePerNight());
            assertEquals("AVAILABLE", paramRoom.getStatus());
        }

        @Test
        @DisplayName("4-arg constructor sets id, type, price, status")
        void testFourArgConstructor() {
            Room fullRoom = new Room(101, "DELUXE", new BigDecimal("35000.00"), "OCCUPIED");
            assertEquals(101, fullRoom.getId());
            assertEquals("DELUXE", fullRoom.getRoomType());
            assertEquals(new BigDecimal("35000.00"), fullRoom.getPricePerNight());
            assertEquals("OCCUPIED", fullRoom.getStatus());
        }
    }

    @Nested
    @DisplayName("Status Constants Tests")
    class StatusConstantsTests {

        @Test
        @DisplayName("STATUS_AVAILABLE is 'AVAILABLE'")
        void testStatusAvailableConstant() {
            assertEquals("AVAILABLE", Room.STATUS_AVAILABLE);
        }

        @Test
        @DisplayName("STATUS_OCCUPIED is 'OCCUPIED'")
        void testStatusOccupiedConstant() {
            assertEquals("OCCUPIED", Room.STATUS_OCCUPIED);
        }

        @Test
        @DisplayName("STATUS_MAINTENANCE is 'MAINTENANCE'")
        void testStatusMaintenanceConstant() {
            assertEquals("MAINTENANCE", Room.STATUS_MAINTENANCE);
        }

        @Test
        @DisplayName("STATUS_RESERVED is 'RESERVED'")
        void testStatusReservedConstant() {
            assertEquals("RESERVED", Room.STATUS_RESERVED);
        }
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Set and get room ID")
        void testSetGetId() {
            room.setId(101);
            assertEquals(101, room.getId());
        }

        @Test
        @DisplayName("Set and get room type")
        void testSetGetRoomType() {
            room.setRoomType("SUITE");
            assertEquals("SUITE", room.getRoomType());
        }

        @Test
        @DisplayName("Set and get price per night")
        void testSetGetPricePerNight() {
            BigDecimal price = new BigDecimal("25000.00");
            room.setPricePerNight(price);
            assertEquals(price, room.getPricePerNight());
        }

        @Test
        @DisplayName("Set and get status")
        void testSetGetStatus() {
            room.setStatus("AVAILABLE");
            assertEquals("AVAILABLE", room.getStatus());
        }
    }

    @Nested
    @DisplayName("Status Check Tests")
    class StatusCheckTests {

        @Test
        @DisplayName("isAvailable() returns true for AVAILABLE status")
        void testIsAvailable_True() {
            room.setStatus("AVAILABLE");
            assertTrue(room.isAvailable());
        }

        @Test
        @DisplayName("isAvailable() is case insensitive")
        void testIsAvailable_CaseInsensitive() {
            room.setStatus("available");
            assertTrue(room.isAvailable());
        }

        @Test
        @DisplayName("isAvailable() returns false for non-AVAILABLE status")
        void testIsAvailable_False() {
            room.setStatus("OCCUPIED");
            assertFalse(room.isAvailable());
        }

        @Test
        @DisplayName("isOccupied() returns true for OCCUPIED status")
        void testIsOccupied_True() {
            room.setStatus("OCCUPIED");
            assertTrue(room.isOccupied());
        }

        @Test
        @DisplayName("isOccupied() returns false for non-OCCUPIED status")
        void testIsOccupied_False() {
            room.setStatus("AVAILABLE");
            assertFalse(room.isOccupied());
        }

        @Test
        @DisplayName("isMaintenance() returns true for MAINTENANCE status")
        void testIsMaintenance_True() {
            room.setStatus("MAINTENANCE");
            assertTrue(room.isMaintenance());
        }

        @Test
        @DisplayName("isMaintenance() returns false for non-MAINTENANCE status")
        void testIsMaintenance_False() {
            room.setStatus("AVAILABLE");
            assertFalse(room.isMaintenance());
        }

        @Test
        @DisplayName("isReserved() returns true for RESERVED status")
        void testIsReserved_True() {
            room.setStatus("RESERVED");
            assertTrue(room.isReserved());
        }

        @Test
        @DisplayName("isReserved() returns false for non-RESERVED status")
        void testIsReserved_False() {
            room.setStatus("AVAILABLE");
            assertFalse(room.isReserved());
        }
    }

    @Nested
    @DisplayName("Equals & HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals() returns true for same id")
        void testEquals_SameId() {
            Room room1 = new Room(1, "SINGLE", new BigDecimal("15000"), "AVAILABLE");
            Room room2 = new Room(1, "DOUBLE", new BigDecimal("25000"), "OCCUPIED");
            assertEquals(room1, room2);
        }

        @Test
        @DisplayName("equals() returns false for different id")
        void testEquals_DifferentId() {
            Room room1 = new Room(1, "SINGLE", new BigDecimal("15000"), "AVAILABLE");
            Room room2 = new Room(2, "SINGLE", new BigDecimal("15000"), "AVAILABLE");
            assertNotEquals(room1, room2);
        }

        @Test
        @DisplayName("hashCode() is consistent for equal objects")
        void testHashCode_Consistent() {
            Room room1 = new Room(1, "SINGLE", new BigDecimal("15000"), "AVAILABLE");
            Room room2 = new Room(1, "DOUBLE", new BigDecimal("25000"), "OCCUPIED");
            assertEquals(room1.hashCode(), room2.hashCode());
        }

        @Test
        @DisplayName("equals() returns false for null")
        void testEquals_Null() {
            Room room1 = new Room(1, "SINGLE", new BigDecimal("15000"), "AVAILABLE");
            assertNotEquals(room1, null);
        }
    }

    @Test
    @DisplayName("toString() contains room information")
    void testToString() {
        Room room1 = new Room(101, "SUITE", new BigDecimal("50000.00"), "AVAILABLE");
        String result = room1.toString();
        assertTrue(result.contains("SUITE"));
        assertTrue(result.contains("50000.00"));
        assertTrue(result.contains("AVAILABLE"));
        assertTrue(result.contains("id=101"));
    }
}
