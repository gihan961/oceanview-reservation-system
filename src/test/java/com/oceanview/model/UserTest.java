package com.oceanview.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Unit Tests for User Model
 * Tests constructors, getters/setters, role checks, equals, hashCode
 */
@DisplayName("User Model Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates empty User")
        void testDefaultConstructor() {
            User defaultUser = new User();
            assertNotNull(defaultUser);
            assertEquals(0, defaultUser.getId());
            assertNull(defaultUser.getUsername());
            assertNull(defaultUser.getPasswordHash());
            assertNull(defaultUser.getRole());
            assertNull(defaultUser.getCreatedAt());
        }

        @Test
        @DisplayName("3-arg constructor sets username, passwordHash, role")
        void testThreeArgConstructor() {
            User paramUser = new User("admin", "hashed123", "ADMIN");
            assertEquals("admin", paramUser.getUsername());
            assertEquals("hashed123", paramUser.getPasswordHash());
            assertEquals("ADMIN", paramUser.getRole());
        }

        @Test
        @DisplayName("5-arg constructor sets all fields including id and createdAt")
        void testFiveArgConstructor() {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            User fullUser = new User(1, "john", "hash456", "MANAGER", now);
            assertEquals(1, fullUser.getId());
            assertEquals("john", fullUser.getUsername());
            assertEquals("hash456", fullUser.getPasswordHash());
            assertEquals("MANAGER", fullUser.getRole());
            assertEquals(now, fullUser.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Set and get user ID")
        void testSetGetId() {
            user.setId(42);
            assertEquals(42, user.getId());
        }

        @Test
        @DisplayName("Set and get username")
        void testSetGetUsername() {
            user.setUsername("testuser");
            assertEquals("testuser", user.getUsername());
        }

        @Test
        @DisplayName("Set and get password hash")
        void testSetGetPasswordHash() {
            user.setPasswordHash("securehash");
            assertEquals("securehash", user.getPasswordHash());
        }

        @Test
        @DisplayName("Set and get role")
        void testSetGetRole() {
            user.setRole("STAFF");
            assertEquals("STAFF", user.getRole());
        }

        @Test
        @DisplayName("Set and get createdAt timestamp")
        void testSetGetCreatedAt() {
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            user.setCreatedAt(ts);
            assertEquals(ts, user.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Role Check Tests")
    class RoleCheckTests {

        @Test
        @DisplayName("isAdmin() returns true for ADMIN role")
        void testIsAdmin_True() {
            user.setRole("ADMIN");
            assertTrue(user.isAdmin());
        }

        @Test
        @DisplayName("isAdmin() returns true for lowercase 'admin'")
        void testIsAdmin_CaseInsensitive() {
            user.setRole("admin");
            assertTrue(user.isAdmin());
        }

        @Test
        @DisplayName("isAdmin() returns false for non-ADMIN role")
        void testIsAdmin_False() {
            user.setRole("STAFF");
            assertFalse(user.isAdmin());
        }

        @Test
        @DisplayName("isManager() returns true for MANAGER role")
        void testIsManager_True() {
            user.setRole("MANAGER");
            assertTrue(user.isManager());
        }

        @Test
        @DisplayName("isManager() returns false for non-MANAGER role")
        void testIsManager_False() {
            user.setRole("ADMIN");
            assertFalse(user.isManager());
        }

        @Test
        @DisplayName("isStaff() returns true for STAFF role")
        void testIsStaff_True() {
            user.setRole("STAFF");
            assertTrue(user.isStaff());
        }

        @Test
        @DisplayName("isStaff() returns false for non-STAFF role")
        void testIsStaff_False() {
            user.setRole("MANAGER");
            assertFalse(user.isStaff());
        }
    }

    @Nested
    @DisplayName("Equals & HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals() returns true for same id and username")
        void testEquals_Same() {
            User user1 = new User(1, "john", "hash1", "ADMIN", null);
            User user2 = new User(1, "john", "hash2", "STAFF", null);
            assertEquals(user1, user2);
        }

        @Test
        @DisplayName("equals() returns false for different id")
        void testEquals_DifferentId() {
            User user1 = new User(1, "john", "hash1", "ADMIN", null);
            User user2 = new User(2, "john", "hash1", "ADMIN", null);
            assertNotEquals(user1, user2);
        }

        @Test
        @DisplayName("equals() returns false for null")
        void testEquals_Null() {
            User user1 = new User(1, "john", "hash1", "ADMIN", null);
            assertNotEquals(user1, null);
        }

        @Test
        @DisplayName("equals() returns true for same object reference")
        void testEquals_SameReference() {
            User user1 = new User(1, "john", "hash1", "ADMIN", null);
            assertEquals(user1, user1);
        }
    }

    @Test
    @DisplayName("toString() contains user information")
    void testToString() {
        User user1 = new User(1, "admin", "hash", "ADMIN", null);
        String result = user1.toString();
        assertTrue(result.contains("admin"));
        assertTrue(result.contains("ADMIN"));
        assertTrue(result.contains("id=1"));
    }
}
