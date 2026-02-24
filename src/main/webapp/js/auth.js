// Authentication and Role-Based Access Control Utilities

// Role permissions configuration — strict RBAC matrix
// Admin: full access
// Manager: generate reports, add/delete rooms, add/cancel reservations, view all
// Staff: view-only (reservations, bookings, available rooms) — no edit/delete/reports
const PERMISSIONS = {
    ADMIN: {
        canViewDashboard: true,
        canManageRooms: true,       // Add new rooms
        canDeleteRooms: true,       // Delete rooms
        canViewRooms: true,         // View available rooms
        canViewReports: true,       // Generate / view reports
        canPrintReports: true,      // Print reports
        canModifyReports: true,     // Modify / edit reports (Admin only)
        canEditFinancials: true,    // Edit financial totals (Admin only)
        canAddReservations: true,   // Create new reservations
        canViewReservations: true,  // View reservations
        canPrintInvoice: true,      // Print invoices
        canEditReservations: true,  // Edit reservations
        canDeleteReservations: true,// Cancel reservations
        canManageAccounts: true     // Create/manage/edit user accounts (Admin only)
    },
    MANAGER: {
        canViewDashboard: true,
        canManageRooms: true,
        canDeleteRooms: true,
        canViewRooms: true,
        canViewReports: true,
        canPrintReports: true,
        canModifyReports: false,
        canEditFinancials: false,
        canAddReservations: true,
        canViewReservations: true,
        canPrintInvoice: true,
        canEditReservations: true,
        canDeleteReservations: true,
        canManageAccounts: false
    },
    STAFF: {
        canViewDashboard: true,
        canManageRooms: false,
        canDeleteRooms: false,
        canViewRooms: true,
        canViewReports: false,
        canPrintReports: false,
        canModifyReports: false,
        canEditFinancials: false,
        canAddReservations: true,
        canViewReservations: true,
        canPrintInvoice: true,
        canEditReservations: false,
        canDeleteReservations: false,
        canManageAccounts: false
    }
};

// Check if user is authenticated
async function checkAuth() {
    try {
        // Use relative path from current location
        const currentPath = window.location.pathname;
        const apiPath = currentPath.includes('/pages/') 
            ? '../api/login' 
            : 'api/login';
        
        const response = await fetch(apiPath, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            console.warn('Auth check failed with status:', response.status);
            return { authenticated: false };
        }
        
        const data = await response.json();
        console.log('Auth check response:', data);
        
        if (data.success && data.loggedIn) {
            return {
                authenticated: true,
                user: data.user,
                role: data.user.role
            };
        } else {
            return { authenticated: false };
        }
    } catch (error) {
        console.error('Authentication check failed:', error);
        return { authenticated: false };
    }
}

// Redirect to login if not authenticated
async function requireAuth() {
    const auth = await checkAuth();
    if (!auth.authenticated) {
        window.location.href = 'login.html';
        return null;
    }
    return auth;
}

// Check if user has specific permission
function hasPermission(role, permission) {
    if (!role || !PERMISSIONS[role.toUpperCase()]) {
        return false;
    }
    return PERMISSIONS[role.toUpperCase()][permission] || false;
}

// Apply role-based UI restrictions
function applyRoleBasedUI(role) {
    if (!role) return;
    
    const upperRole = role.toUpperCase();
    const permissions = PERMISSIONS[upperRole];
    
    if (!permissions) return;
    
    // Hide/show navigation items
    document.querySelectorAll('[data-permission]').forEach(element => {
        const requiredPermission = element.getAttribute('data-permission');
        if (!permissions[requiredPermission]) {
            element.style.display = 'none';
        } else {
            element.style.display = '';
        }
    });
    
    // Disable buttons/links
    document.querySelectorAll('[data-requires]').forEach(element => {
        const requiredPermission = element.getAttribute('data-requires');
        if (!permissions[requiredPermission]) {
            element.disabled = true;
            element.classList.add('disabled');
            element.style.pointerEvents = 'none';
            element.style.opacity = '0.5';
            element.title = 'You do not have permission for this action';
        }
    });
    
    // Show role-specific content
    document.querySelectorAll('[data-role]').forEach(element => {
        const allowedRoles = element.getAttribute('data-role').split(',');
        if (!allowedRoles.includes(upperRole)) {
            element.style.display = 'none';
        }
    });
}

// Display user info in header
function displayUserInfo(user) {
    const usernameElement = document.getElementById('username');
    const roleElement = document.getElementById('userRole');
    
    if (usernameElement) {
        usernameElement.textContent = user.username;
    }
    
    if (roleElement) {
        roleElement.textContent = user.role;
        roleElement.className = 'role-badge role-' + user.role.toLowerCase();
    }
}

// Initialize page with authentication and role-based access
async function initializePageAuth() {
    const auth = await requireAuth();
    if (!auth) return null;
    
    displayUserInfo(auth.user);
    applyRoleBasedUI(auth.role);
    
    return auth;
}

// Logout function
async function logout() {
    try {
        const response = await fetch('../api/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        
        if (data.success) {
            window.location.href = 'login.html';
        } else {
            console.error('Logout failed:', data.message);
            // Redirect anyway
            window.location.href = 'login.html';
        }
    } catch (error) {
        console.error('Logout error:', error);
        // Redirect anyway
        window.location.href = 'login.html';
    }
}
