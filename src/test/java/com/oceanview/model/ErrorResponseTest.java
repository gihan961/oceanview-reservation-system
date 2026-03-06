package com.oceanview.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Unit Tests for ErrorResponse Model
 * Tests constructors, builder pattern, error list management
 */
@DisplayName("ErrorResponse Model Tests")
class ErrorResponseTest {

    private ErrorResponse errorResponse;

    @BeforeEach
    void setUp() {
        errorResponse = new ErrorResponse();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor sets success=false and timestamp")
        void testDefaultConstructor() {
            assertFalse(errorResponse.isSuccess());
            assertNotNull(errorResponse.getTimestamp());
            assertNotNull(errorResponse.getErrors());
            assertTrue(errorResponse.getErrors().isEmpty());
        }

        @Test
        @DisplayName("3-arg constructor sets errorCode, message, status")
        void testThreeArgConstructor() {
            ErrorResponse er = new ErrorResponse("VALIDATION_ERROR", "Invalid input", 400);
            assertEquals("VALIDATION_ERROR", er.getErrorCode());
            assertEquals("Invalid input", er.getMessage());
            assertEquals(400, er.getStatus());
            assertFalse(er.isSuccess());
        }

        @Test
        @DisplayName("4-arg constructor sets errorCode, message, status, path")
        void testFourArgConstructor() {
            ErrorResponse er = new ErrorResponse("NOT_FOUND", "Room not found", 404, "/api/rooms/999");
            assertEquals("NOT_FOUND", er.getErrorCode());
            assertEquals("Room not found", er.getMessage());
            assertEquals(404, er.getStatus());
            assertEquals("/api/rooms/999", er.getPath());
        }
    }

    @Nested
    @DisplayName("Error List Management Tests")
    class ErrorListTests {

        @Test
        @DisplayName("addError() adds error to list")
        void testAddError() {
            errorResponse.addError("Field 'name' is required");
            assertEquals(1, errorResponse.getErrors().size());
            assertEquals("Field 'name' is required", errorResponse.getErrors().get(0));
        }

        @Test
        @DisplayName("addError() adds multiple errors")
        void testAddMultipleErrors() {
            errorResponse.addError("Error 1");
            errorResponse.addError("Error 2");
            errorResponse.addError("Error 3");
            assertEquals(3, errorResponse.getErrors().size());
        }

        @Test
        @DisplayName("setErrors() replaces entire error list")
        void testSetErrors() {
            errorResponse.setErrors(Arrays.asList("Error A", "Error B"));
            assertEquals(2, errorResponse.getErrors().size());
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderTests {

        @Test
        @DisplayName("Builder creates ErrorResponse with all fields")
        void testBuilderComplete() {
            ErrorResponse er = new ErrorResponse.Builder()
                    .errorCode("AUTH_ERROR")
                    .message("Unauthorized")
                    .status(401)
                    .path("/api/login")
                    .addError("Invalid credentials")
                    .stackTrace("com.oceanview.AuthService.login()")
                    .build();

            assertEquals("AUTH_ERROR", er.getErrorCode());
            assertEquals("Unauthorized", er.getMessage());
            assertEquals(401, er.getStatus());
            assertEquals("/api/login", er.getPath());
            assertEquals(1, er.getErrors().size());
            assertNotNull(er.getStackTrace());
        }

        @Test
        @DisplayName("Builder creates ErrorResponse with minimal fields")
        void testBuilderMinimal() {
            ErrorResponse er = new ErrorResponse.Builder()
                    .message("Something went wrong")
                    .status(500)
                    .build();

            assertEquals("Something went wrong", er.getMessage());
            assertEquals(500, er.getStatus());
        }
    }

    @Test
    @DisplayName("Getter and setter for stackTrace")
    void testSetGetStackTrace() {
        errorResponse.setStackTrace("java.lang.NullPointerException\n\tat com.oceanview...");
        assertNotNull(errorResponse.getStackTrace());
        assertTrue(errorResponse.getStackTrace().contains("NullPointerException"));
    }
}
