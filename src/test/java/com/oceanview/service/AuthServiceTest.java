package com.oceanview.service;

import com.oceanview.dao.UserDAO;
import com.oceanview.exception.AuthenticationException;
import com.oceanview.exception.ValidationException;
import com.oceanview.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD Unit Tests for AuthService.
 * Uses Mockito to mock UserDAO — isolated unit testing of business logic.
 * 36 test cases covering login, user creation, registration, password change, role checks.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserDAO mockUserDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(mockUserDAO);
    }

    // ========== LOGIN TESTS ==========

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("TC-AS-01: Login with valid credentials returns user")
        void testLoginSuccess() throws Exception {
            User expectedUser = new User(1, "admin", "hashed", "ADMIN", null);
            when(mockUserDAO.authenticate(eq("admin"), anyString())).thenReturn(expectedUser);

            User result = authService.login("admin", "password123");

            assertNotNull(result);
            assertEquals("admin", result.getUsername());
            assertEquals("ADMIN", result.getRole());
            verify(mockUserDAO).authenticate(eq("admin"), anyString());
        }

        @Test
        @DisplayName("TC-AS-02: Login with invalid credentials throws AuthenticationException")
        void testLoginInvalidCredentials() throws Exception {
            when(mockUserDAO.authenticate(anyString(), anyString())).thenReturn(null);

            assertThrows(AuthenticationException.class,
                    () -> authService.login("admin", "wrongpassword"));
        }

        @Test
        @DisplayName("TC-AS-03: Login with empty username throws ValidationException")
        void testLoginEmptyUsername() {
            assertThrows(ValidationException.class,
                    () -> authService.login("", "password123"));
        }

        @Test
        @DisplayName("TC-AS-04: Login with null username throws ValidationException")
        void testLoginNullUsername() {
            assertThrows(ValidationException.class,
                    () -> authService.login(null, "password123"));
        }

        @Test
        @DisplayName("TC-AS-05: Login with empty password throws ValidationException")
        void testLoginEmptyPassword() {
            assertThrows(ValidationException.class,
                    () -> authService.login("admin", ""));
        }

        @Test
        @DisplayName("TC-AS-06: Login with null password throws ValidationException")
        void testLoginNullPassword() {
            assertThrows(ValidationException.class,
                    () -> authService.login("admin", null));
        }

        @Test
        @DisplayName("TC-AS-07: Login with short username (<3 chars) throws ValidationException")
        void testLoginShortUsername() {
            assertThrows(ValidationException.class,
                    () -> authService.login("ab", "password123"));
        }

        @Test
        @DisplayName("TC-AS-08: Login with database error throws AuthenticationException")
        void testLoginDatabaseError() throws Exception {
            when(mockUserDAO.authenticate(anyString(), anyString()))
                    .thenThrow(new SQLException("DB connection failed"));

            assertThrows(AuthenticationException.class,
                    () -> authService.login("admin", "password123"));
        }
    }

    // ========== CREATE USER TESTS ==========

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("TC-AS-09: Create user with valid data succeeds")
        void testCreateUserSuccess() throws Exception {
            when(mockUserDAO.findByUsername("newuser")).thenReturn(null);
            when(mockUserDAO.create(any(User.class))).thenReturn(true);

            User result = authService.createUser("newuser", "password123", "STAFF");

            assertNotNull(result);
            assertEquals("newuser", result.getUsername());
            assertEquals("STAFF", result.getRole());
            verify(mockUserDAO).create(any(User.class));
        }

        @Test
        @DisplayName("TC-AS-10: Create user with duplicate username throws ValidationException")
        void testCreateUserDuplicateUsername() throws Exception {
            when(mockUserDAO.findByUsername("existing"))
                    .thenReturn(new User("existing", "hash", "STAFF"));

            assertThrows(ValidationException.class,
                    () -> authService.createUser("existing", "password123", "STAFF"));
        }

        @Test
        @DisplayName("TC-AS-11: Create user with short password (<6 chars) throws ValidationException")
        void testCreateUserShortPassword() {
            assertThrows(ValidationException.class,
                    () -> authService.createUser("newuser", "12345", "STAFF"));
        }

        @Test
        @DisplayName("TC-AS-12: Create user with invalid role throws ValidationException")
        void testCreateUserInvalidRole() {
            assertThrows(ValidationException.class,
                    () -> authService.createUser("newuser", "password123", "INVALID"));
        }

        @Test
        @DisplayName("TC-AS-13: Create user with empty role throws ValidationException")
        void testCreateUserEmptyRole() {
            assertThrows(ValidationException.class,
                    () -> authService.createUser("newuser", "password123", ""));
        }
    }

    // ========== REGISTER USER TESTS ==========

    @Nested
    @DisplayName("Register User Tests")
    class RegisterUserTests {

        @Test
        @DisplayName("TC-AS-14: Register user with STAFF role succeeds")
        void testRegisterUserStaffSuccess() throws Exception {
            when(mockUserDAO.findByUsername("staffuser")).thenReturn(null);
            when(mockUserDAO.create(any(User.class))).thenReturn(true);

            User result = authService.registerUser("staffuser", "password123", "STAFF", "John Smith");

            assertNotNull(result);
            assertEquals("staffuser", result.getUsername());
            assertEquals("STAFF", result.getRole());
        }

        @Test
        @DisplayName("TC-AS-15: Register user with MANAGER role succeeds")
        void testRegisterUserManagerSuccess() throws Exception {
            when(mockUserDAO.findByUsername("mgruser")).thenReturn(null);
            when(mockUserDAO.create(any(User.class))).thenReturn(true);

            User result = authService.registerUser("mgruser", "password123", "MANAGER", "Jane Doe");

            assertNotNull(result);
            assertEquals("MANAGER", result.getRole());
        }

        @Test
        @DisplayName("TC-AS-16: Register as ADMIN throws ValidationException")
        void testRegisterAsAdminBlocked() {
            assertThrows(ValidationException.class,
                    () -> authService.registerUser("adminuser", "password123", "ADMIN", "Admin User"));
        }

        @Test
        @DisplayName("TC-AS-17: Register with duplicate username throws IllegalStateException")
        void testRegisterDuplicateUsername() throws Exception {
            when(mockUserDAO.findByUsername("existing"))
                    .thenReturn(new User("existing", "hash", "STAFF"));

            assertThrows(IllegalStateException.class,
                    () -> authService.registerUser("existing", "password123", "STAFF", "Existing User"));
        }

        @Test
        @DisplayName("TC-AS-18: Register with empty fullName throws ValidationException")
        void testRegisterEmptyFullName() {
            assertThrows(ValidationException.class,
                    () -> authService.registerUser("newuser", "password123", "STAFF", ""));
        }

        @Test
        @DisplayName("TC-AS-19: Register with short fullName (<2 chars) throws ValidationException")
        void testRegisterShortFullName() {
            assertThrows(ValidationException.class,
                    () -> authService.registerUser("newuser", "password123", "STAFF", "J"));
        }

        @Test
        @DisplayName("TC-AS-20: Register with invalid role throws ValidationException")
        void testRegisterInvalidRole() {
            assertThrows(ValidationException.class,
                    () -> authService.registerUser("newuser", "password123", "SUPERUSER", "John Doe"));
        }

        @Test
        @DisplayName("TC-AS-21: Register with short password (<6 chars) throws ValidationException")
        void testRegisterShortPassword() {
            assertThrows(ValidationException.class,
                    () -> authService.registerUser("newuser", "12345", "STAFF", "John Doe"));
        }
    }

    // ========== CHANGE PASSWORD TESTS ==========

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("TC-AS-22: Change password with correct old password succeeds")
        void testChangePasswordSuccess() throws Exception {
            // AuthService.hashPassword() uses String.hashCode()
            String oldPasswordHash = String.valueOf("oldpass123".hashCode());
            User existingUser = new User(1, "user1", oldPasswordHash, "STAFF", null);
            when(mockUserDAO.findById(1)).thenReturn(existingUser);
            when(mockUserDAO.update(any(User.class))).thenReturn(true);

            boolean result = authService.changePassword(1, "oldpass123", "newpass123");

            assertTrue(result);
            verify(mockUserDAO).update(any(User.class));
        }

        @Test
        @DisplayName("TC-AS-23: Change password with wrong old password throws AuthenticationException")
        void testChangePasswordWrongOldPassword() throws Exception {
            User existingUser = new User(1, "user1", "correcthash", "STAFF", null);
            when(mockUserDAO.findById(1)).thenReturn(existingUser);

            assertThrows(AuthenticationException.class,
                    () -> authService.changePassword(1, "wrongoldpass", "newpass123"));
        }

        @Test
        @DisplayName("TC-AS-24: Change password with short new password (<6 chars) throws ValidationException")
        void testChangePasswordShortNewPassword() {
            assertThrows(ValidationException.class,
                    () -> authService.changePassword(1, "oldpass", "12345"));
        }

        @Test
        @DisplayName("TC-AS-25: Change password for non-existent user throws ValidationException")
        void testChangePasswordUserNotFound() throws Exception {
            when(mockUserDAO.findById(999)).thenReturn(null);

            assertThrows(ValidationException.class,
                    () -> authService.changePassword(999, "oldpass", "newpass123"));
        }

        @Test
        @DisplayName("TC-AS-26: Change password with empty new password throws ValidationException")
        void testChangePasswordEmptyNewPassword() {
            assertThrows(ValidationException.class,
                    () -> authService.changePassword(1, "oldpass", ""));
        }
    }

    // ========== ROLE CHECK TESTS ==========

    @Nested
    @DisplayName("Role Check Tests")
    class RoleCheckTests {

        @Test
        @DisplayName("TC-AS-27: hasRole() returns true for matching role")
        void testHasRoleTrue() {
            User user = new User("admin", "hash", "ADMIN");
            assertTrue(authService.hasRole(user, "ADMIN"));
        }

        @Test
        @DisplayName("TC-AS-28: hasRole() case-insensitive match")
        void testHasRoleCaseInsensitive() {
            User user = new User("admin", "hash", "ADMIN");
            assertTrue(authService.hasRole(user, "admin"));
        }

        @Test
        @DisplayName("TC-AS-29: hasRole() returns false for non-matching role")
        void testHasRoleFalse() {
            User user = new User("staff", "hash", "STAFF");
            assertFalse(authService.hasRole(user, "ADMIN"));
        }

        @Test
        @DisplayName("TC-AS-30: hasRole() returns false for null user")
        void testHasRoleNullUser() {
            assertFalse(authService.hasRole(null, "ADMIN"));
        }

        @Test
        @DisplayName("TC-AS-31: hasRole() returns false for null role")
        void testHasRoleNullRole() {
            User user = new User("admin", "hash", "ADMIN");
            assertFalse(authService.hasRole(user, null));
        }

        @Test
        @DisplayName("TC-AS-32: isAdmin() returns true for admin user")
        void testIsAdminTrue() {
            User user = new User("admin", "hash", "ADMIN");
            assertTrue(authService.isAdmin(user));
        }

        @Test
        @DisplayName("TC-AS-33: isAdmin() returns false for non-admin user")
        void testIsAdminFalse() {
            User user = new User("staff", "hash", "STAFF");
            assertFalse(authService.isAdmin(user));
        }

        @Test
        @DisplayName("TC-AS-34: isAdmin() returns false for null user")
        void testIsAdminNullUser() {
            assertFalse(authService.isAdmin(null));
        }

        @Test
        @DisplayName("TC-AS-35: isManager() returns true for manager user")
        void testIsManagerTrue() {
            User user = new User("mgr", "hash", "MANAGER");
            assertTrue(authService.isManager(user));
        }

        @Test
        @DisplayName("TC-AS-36: isManager() returns false for non-manager user")
        void testIsManagerFalse() {
            User user = new User("staff", "hash", "STAFF");
            assertFalse(authService.isManager(user));
        }
    }
}
