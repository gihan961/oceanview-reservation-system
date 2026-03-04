const PERMISSIONS = {
    ADMIN: {
        canViewDashboard: true,
        canManageRooms: true,
        canDeleteRooms: true,
        canViewRooms: true,
        canViewReports: true,
        canPrintReports: true,
        canModifyReports: true,
        canEditFinancials: true,
        canAddReservations: true,
        canViewReservations: true,
        canPrintInvoice: true,
        canEditReservations: true,
        canDeleteReservations: true,
        canManageAccounts: true
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

async function checkAuth() {
    try {

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

async function requireAuth() {
    const auth = await checkAuth();
    if (!auth.authenticated) {
        window.location.href = 'login.html';
        return null;
    }
    return auth;
}

function hasPermission(role, permission) {
    if (!role || !PERMISSIONS[role.toUpperCase()]) {
        return false;
    }
    return PERMISSIONS[role.toUpperCase()][permission] || false;
}

function applyRoleBasedUI(role) {
    if (!role) return;

    const upperRole = role.toUpperCase();
    const permissions = PERMISSIONS[upperRole];

    if (!permissions) return;

    document.querySelectorAll('[data-permission]').forEach(element => {
        const requiredPermission = element.getAttribute('data-permission');
        if (!permissions[requiredPermission]) {
            element.style.display = 'none';
        } else {
            element.style.display = '';
        }
    });

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

    document.querySelectorAll('[data-role]').forEach(element => {
        const allowedRoles = element.getAttribute('data-role').split(',');
        if (!allowedRoles.includes(upperRole)) {
            element.style.display = 'none';
        }
    });
}

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

async function initializePageAuth() {
    const auth = await requireAuth();
    if (!auth) return null;

    displayUserInfo(auth.user);
    applyRoleBasedUI(auth.role);

    return auth;
}

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

            window.location.href = 'login.html';
        }
    } catch (error) {
        console.error('Logout error:', error);

        window.location.href = 'login.html';
    }
}
