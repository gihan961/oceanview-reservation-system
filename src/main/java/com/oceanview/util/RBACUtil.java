package com.oceanview.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Role-Based Access Control Utility
 * Centralised permission checks for all API endpoints.
 *
 * Roles: ADMIN, MANAGER, STAFF
 *
 */
public final class RBACUtil {

    private static final Logger LOGGER = Logger.getLogger(RBACUtil.class.getName());

    private RBACUtil() {
        // utility class – no instances
    }

    // ─── Role Constants ───────────────────────────────────────────────

    public static final String ROLE_ADMIN   = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_STAFF   = "STAFF";

    // ─── Authentication Check ─────────────────────────────────────────

    /**
     * Check whether the request has a valid session with a logged-in user.
     *
     * @return true if authenticated
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    // ─── Role Retrieval ───────────────────────────────────────────────

    /**
     * Get the role string from the session (upper-cased).
     *
     * @return the role, or null if not logged in
     */
    public static String getRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object role = session.getAttribute("role");
        return role != null ? role.toString().toUpperCase() : null;
    }

    // ─── Role Checks ──────────────────────────────────────────────────

    public static boolean isAdmin(HttpServletRequest request) {
        return ROLE_ADMIN.equalsIgnoreCase(getRole(request));
    }

    public static boolean isManager(HttpServletRequest request) {
        return ROLE_MANAGER.equalsIgnoreCase(getRole(request));
    }

    public static boolean isStaff(HttpServletRequest request) {
        return ROLE_STAFF.equalsIgnoreCase(getRole(request));
    }

    public static boolean isAdminOrManager(HttpServletRequest request) {
        String role = getRole(request);
        return ROLE_ADMIN.equalsIgnoreCase(role) || ROLE_MANAGER.equalsIgnoreCase(role);
    }

    /**
     * Check if the current user's role is one of the allowed roles.
     *
     * @param request       the HTTP request
     * @param allowedRoles  one or more role strings (e.g. "ADMIN", "MANAGER")
     * @return true if the user's role matches any of the allowed roles
     */
    public static boolean hasRole(HttpServletRequest request, String... allowedRoles) {
        String userRole = getRole(request);
        if (userRole == null) return false;
        return Arrays.stream(allowedRoles)
                     .anyMatch(r -> r.equalsIgnoreCase(userRole));
    }

    // ─── Guard Methods (send 401/403 and return false) ────────────────

    /**
     * Require the user to be authenticated.
     * If not, writes a 401 JSON response and returns false.
     */
    public static boolean requireAuth(HttpServletRequest request,
                                      HttpServletResponse response) throws IOException {
        if (isAuthenticated(request)) return true;

        sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                  "Unauthorized - Please login");
        return false;
    }

    /**
     * Require the user to hold one of the specified roles.
     * Assumes authentication has already been verified.
     * If the role check fails, writes a 403 JSON response and returns false.
     *
     * @param allowedRoles  e.g. ROLE_ADMIN, ROLE_MANAGER
     */
    public static boolean requireRole(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String... allowedRoles) throws IOException {
        if (hasRole(request, allowedRoles)) return true;

        String userRole = getRole(request);
        LOGGER.warning("Access denied for role '" + userRole
                       + "' – required: " + Arrays.toString(allowedRoles));

        sendError(response, HttpServletResponse.SC_FORBIDDEN,
                  "Access Denied - You do not have permission for this action");
        return false;
    }

    /**
     * Combined convenience: require authentication AND one of the given roles.
     * Returns true only if both checks pass; otherwise the response is already
     * written (401 or 403).
     */
    public static boolean requireAuthAndRole(HttpServletRequest request,
                                             HttpServletResponse response,
                                             String... allowedRoles) throws IOException {
        return requireAuth(request, response)
            && requireRole(request, response, allowedRoles);
    }

    // ─── JSON Error Response ──────────────────────────────────────────

    private static void sendError(HttpServletResponse response,
                                  int status, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        out.print("{\"success\": false, \"message\": \""
                  + escapeJson(message) + "\"}");
        out.flush();
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
