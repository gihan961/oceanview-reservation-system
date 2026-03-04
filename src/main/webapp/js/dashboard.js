let currentAuth = null;

document.addEventListener('DOMContentLoaded', async function() {

    currentAuth = await initializePageAuth();
    if (!currentAuth) return;

    await loadDashboardData();
});

async function loadDashboardData() {
    try {
        const response = await fetch('../api/dashboard', { credentials: 'include' });

        if (response.status === 401) {
            window.location.href = 'login.html';
            return;
        }

        const data = await response.json();
        console.log('Dashboard API response:', data);

        if (data.success) {
            updateStats(data.stats);
            updateRecentReservations(data.recentCheckIns);
        } else {
            console.error('Failed to load dashboard data:', data.message);
            showError('Failed to load dashboard data: ' + (data.message || 'Unknown error'));
        }
    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showError('Failed to load dashboard data. Please refresh the page.');
    }
}

function updateStats(stats) {
    if (!stats) return;

    document.getElementById('totalReservations').textContent = stats.totalReservations || 0;
    document.getElementById('activeReservations').textContent = stats.activeReservations || 0;
    document.getElementById('totalRevenue').textContent = formatCurrency(stats.totalRevenue || 0);

    const occupancyRate = stats.totalRooms > 0
        ? ((stats.occupiedRooms / stats.totalRooms) * 100).toFixed(1)
        : 0;
    document.getElementById('occupancyRate').textContent = occupancyRate + '%';
}

function updateRecentReservations(reservations) {
    const tbody = document.querySelector('#recentReservations tbody');

    if (!reservations || reservations.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="no-data">No recent reservations</td></tr>';
        return;
    }

    const role = currentAuth ? currentAuth.role : null;

    tbody.innerHTML = reservations.map(reservation => {

        let actionBtns = `<button class="btn-sm" onclick="viewReservation(${reservation.reservationId})">View</button>`;
        if (!role || hasPermission(role, 'canPrintInvoice')) {
            actionBtns += ` <button class="btn-sm" onclick="printInvoice(${reservation.reservationId})">Invoice</button>`;
        }

        return `
        <tr>
            <td>${escapeHtml(reservation.reservationNumber || String(reservation.reservationId))}</td>
            <td>${escapeHtml(reservation.guestName)}</td>
            <td>${formatDate(reservation.checkInDate)}</td>
            <td>${formatDate(reservation.checkOutDate)}</td>
            <td>${escapeHtml(reservation.roomType || 'N/A')}</td>
            <td><span class="status-badge status-${getStatusClass(reservation.status)}">${escapeHtml(reservation.status)}</span></td>
            <td>${actionBtns}</td>
        </tr>
    `}).join('');
}

function viewReservation(reservationId) {
    window.location.href = `view-reservation.html?id=${reservationId}`;
}

function printInvoice(reservationId) {
    window.location.href = `invoice.html?id=${reservationId}`;
}

function getStatusClass(status) {
    switch (status?.toLowerCase()) {
        case 'confirmed':
            return 'confirmed';
        case 'checked-in':
            return 'active';
        case 'checked-out':
            return 'completed';
        case 'cancelled':
            return 'cancelled';
        default:
            return 'pending';
    }
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function formatCurrency(amount) {
    const num = parseFloat(amount || 0);
    return 'LKR ' + new Intl.NumberFormat('en-LK', {
        style: 'decimal',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(num);
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showError(message) {
    const mainContent = document.querySelector('.main-content');
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-error';
    errorDiv.textContent = message;
    mainContent.insertBefore(errorDiv, mainContent.firstChild);

    setTimeout(() => errorDiv.remove(), 5000);
}
