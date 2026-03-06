package com.oceanview.service;

import com.oceanview.dao.RoomDAO;
import com.oceanview.exception.ValidationException;
import com.oceanview.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD Unit Tests for RoomService.
 * Uses Mockito to mock RoomDAO — isolated unit testing of room management logic.
 * 22 test cases covering CRUD, validation, status management, availability checks.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoomService Tests")
class RoomServiceTest {

    @Mock
    private RoomDAO mockRoomDAO;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(mockRoomDAO);
    }

    // ========== CREATE ROOM TESTS ==========

    @Nested
    @DisplayName("Create Room Tests")
    class CreateRoomTests {

        @Test
        @DisplayName("TC-RS-01: Create room with valid data succeeds")
        void testCreateRoomSuccess() throws Exception {
            when(mockRoomDAO.create(any(Room.class))).thenReturn(true);

            Room result = roomService.createRoom("DELUXE", new BigDecimal("15000.00"), "AVAILABLE");

            assertNotNull(result);
            assertEquals("DELUXE", result.getRoomType());
            assertEquals(new BigDecimal("15000.00"), result.getPricePerNight());
            assertEquals("AVAILABLE", result.getStatus());
            verify(mockRoomDAO).create(any(Room.class));
        }

        @Test
        @DisplayName("TC-RS-02: Create room with null type throws ValidationException")
        void testCreateRoomNullType() {
            assertThrows(ValidationException.class,
                    () -> roomService.createRoom(null, new BigDecimal("15000"), "AVAILABLE"));
        }

        @Test
        @DisplayName("TC-RS-03: Create room with empty type throws ValidationException")
        void testCreateRoomEmptyType() {
            assertThrows(ValidationException.class,
                    () -> roomService.createRoom("", new BigDecimal("15000"), "AVAILABLE"));
        }

        @Test
        @DisplayName("TC-RS-04: Create room with null price throws ValidationException")
        void testCreateRoomNullPrice() {
            assertThrows(ValidationException.class,
                    () -> roomService.createRoom("DELUXE", null, "AVAILABLE"));
        }

        @Test
        @DisplayName("TC-RS-05: Create room with zero price throws ValidationException")
        void testCreateRoomZeroPrice() {
            assertThrows(ValidationException.class,
                    () -> roomService.createRoom("DELUXE", BigDecimal.ZERO, "AVAILABLE"));
        }

        @Test
        @DisplayName("TC-RS-06: Create room with negative price throws ValidationException")
        void testCreateRoomNegativePrice() {
            assertThrows(ValidationException.class,
                    () -> roomService.createRoom("DELUXE", new BigDecimal("-500"), "AVAILABLE"));
        }

        @Test
        @DisplayName("TC-RS-07: Create room with invalid status throws ValidationException")
        void testCreateRoomInvalidStatus() {
            assertThrows(ValidationException.class,
                    () -> roomService.createRoom("DELUXE", new BigDecimal("15000"), "INVALID"));
        }

        @Test
        @DisplayName("TC-RS-08: Create room with empty status throws ValidationException")
        void testCreateRoomEmptyStatus() {
            assertThrows(ValidationException.class,
                    () -> roomService.createRoom("DELUXE", new BigDecimal("15000"), ""));
        }

        @Test
        @DisplayName("TC-RS-09: Create room when DAO fails throws ValidationException")
        void testCreateRoomDAOFailure() throws Exception {
            when(mockRoomDAO.create(any(Room.class))).thenReturn(false);

            assertThrows(ValidationException.class,
                    () -> roomService.createRoom("DELUXE", new BigDecimal("15000"), "AVAILABLE"));
        }

        @Test
        @DisplayName("TC-RS-10: Create room with database error throws ValidationException")
        void testCreateRoomDatabaseError() throws Exception {
            when(mockRoomDAO.create(any(Room.class))).thenThrow(new SQLException("DB error"));

            assertThrows(ValidationException.class,
                    () -> roomService.createRoom("DELUXE", new BigDecimal("15000"), "AVAILABLE"));
        }
    }

    // ========== UPDATE ROOM TESTS ==========

    @Nested
    @DisplayName("Update Room Tests")
    class UpdateRoomTests {

        @Test
        @DisplayName("TC-RS-11: Update room with valid data succeeds")
        void testUpdateRoomSuccess() throws Exception {
            Room room = new Room("SUITE", new BigDecimal("25000.00"), "AVAILABLE");
            room.setId(1);
            when(mockRoomDAO.update(any(Room.class))).thenReturn(true);

            boolean result = roomService.updateRoom(room);

            assertTrue(result);
            verify(mockRoomDAO).update(any(Room.class));
        }

        @Test
        @DisplayName("TC-RS-12: Update room with null throws ValidationException")
        void testUpdateRoomNull() {
            assertThrows(ValidationException.class,
                    () -> roomService.updateRoom(null));
        }
    }

    // ========== DELETE ROOM TESTS ==========

    @Nested
    @DisplayName("Delete Room Tests")
    class DeleteRoomTests {

        @Test
        @DisplayName("TC-RS-13: Delete room with valid ID succeeds")
        void testDeleteRoomSuccess() throws Exception {
            when(mockRoomDAO.delete(1)).thenReturn(true);

            boolean result = roomService.deleteRoom(1);

            assertTrue(result);
            verify(mockRoomDAO).delete(1);
        }

        @Test
        @DisplayName("TC-RS-14: Delete room with invalid ID (0) throws ValidationException")
        void testDeleteRoomInvalidId() {
            assertThrows(ValidationException.class,
                    () -> roomService.deleteRoom(0));
        }

        @Test
        @DisplayName("TC-RS-15: Delete room with negative ID throws ValidationException")
        void testDeleteRoomNegativeId() {
            assertThrows(ValidationException.class,
                    () -> roomService.deleteRoom(-1));
        }
    }

    // ========== GET ROOM TESTS ==========

    @Nested
    @DisplayName("Get Room Tests")
    class GetRoomTests {

        @Test
        @DisplayName("TC-RS-16: Get room by valid ID returns room")
        void testGetRoomByIdSuccess() throws Exception {
            Room expectedRoom = new Room("DELUXE", new BigDecimal("15000"), "AVAILABLE");
            expectedRoom.setId(1);
            when(mockRoomDAO.findById(1)).thenReturn(expectedRoom);

            Room result = roomService.getRoomById(1);

            assertNotNull(result);
            assertEquals("DELUXE", result.getRoomType());
            assertEquals(1, result.getId());
        }

        @Test
        @DisplayName("TC-RS-17: Get room by invalid ID throws ValidationException")
        void testGetRoomByInvalidId() {
            assertThrows(ValidationException.class,
                    () -> roomService.getRoomById(0));
        }

        @Test
        @DisplayName("TC-RS-18: Get all rooms returns list")
        void testGetAllRooms() throws Exception {
            List<Room> rooms = Arrays.asList(
                    new Room("DELUXE", new BigDecimal("15000"), "AVAILABLE"),
                    new Room("SUITE", new BigDecimal("25000"), "OCCUPIED")
            );
            when(mockRoomDAO.findAll()).thenReturn(rooms);

            List<Room> result = roomService.getAllRooms();

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("TC-RS-19: Get available rooms returns only available")
        void testGetAvailableRooms() throws Exception {
            List<Room> rooms = Collections.singletonList(
                    new Room("DELUXE", new BigDecimal("15000"), "AVAILABLE")
            );
            when(mockRoomDAO.findByStatus(Room.STATUS_AVAILABLE)).thenReturn(rooms);

            List<Room> result = roomService.getAvailableRooms();

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(mockRoomDAO).findByStatus(Room.STATUS_AVAILABLE);
        }

        @Test
        @DisplayName("TC-RS-20: Get rooms by empty status throws ValidationException")
        void testGetRoomsByEmptyStatus() {
            assertThrows(ValidationException.class,
                    () -> roomService.getRoomsByStatus(""));
        }

        @Test
        @DisplayName("TC-RS-21: Get rooms by null type throws ValidationException")
        void testGetRoomsByNullType() {
            assertThrows(ValidationException.class,
                    () -> roomService.getRoomsByType(null));
        }
    }

    // ========== ROOM STATUS & AVAILABILITY TESTS ==========

    @Nested
    @DisplayName("Room Status & Availability Tests")
    class StatusTests {

        @Test
        @DisplayName("TC-RS-22: Update room status with valid inputs succeeds")
        void testUpdateRoomStatusSuccess() throws Exception {
            when(mockRoomDAO.updateStatus(1, "OCCUPIED")).thenReturn(true);

            boolean result = roomService.updateRoomStatus(1, "OCCUPIED");

            assertTrue(result);
            verify(mockRoomDAO).updateStatus(1, "OCCUPIED");
        }

        @Test
        @DisplayName("TC-RS-23: Update room status with invalid ID throws ValidationException")
        void testUpdateRoomStatusInvalidId() {
            assertThrows(ValidationException.class,
                    () -> roomService.updateRoomStatus(0, "AVAILABLE"));
        }

        @Test
        @DisplayName("TC-RS-24: Update room status with invalid status throws ValidationException")
        void testUpdateRoomStatusInvalidStatus() {
            assertThrows(ValidationException.class,
                    () -> roomService.updateRoomStatus(1, "INVALID_STATUS"));
        }

        @Test
        @DisplayName("TC-RS-25: isRoomAvailable returns true for available room")
        void testIsRoomAvailableTrue() throws Exception {
            Room room = new Room("DELUXE", new BigDecimal("15000"), "AVAILABLE");
            room.setId(1);
            when(mockRoomDAO.findById(1)).thenReturn(room);

            boolean result = roomService.isRoomAvailable(1);

            assertTrue(result);
        }

        @Test
        @DisplayName("TC-RS-26: isRoomAvailable returns false for occupied room")
        void testIsRoomAvailableFalse() throws Exception {
            Room room = new Room("DELUXE", new BigDecimal("15000"), "OCCUPIED");
            room.setId(1);
            when(mockRoomDAO.findById(1)).thenReturn(room);

            boolean result = roomService.isRoomAvailable(1);

            assertFalse(result);
        }

        @Test
        @DisplayName("TC-RS-27: isRoomAvailable for non-existent room throws ValidationException")
        void testIsRoomAvailableNotFound() throws Exception {
            when(mockRoomDAO.findById(999)).thenReturn(null);

            assertThrows(ValidationException.class,
                    () -> roomService.isRoomAvailable(999));
        }
    }
}
