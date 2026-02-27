// Available Rooms Page JavaScript
let currentAuth = null;
let allRooms = []; // Store all rooms for filtering

document.addEventListener('DOMContentLoaded', async function() {
    // Initialize authentication
    currentAuth = await initializePageAuth();
    if (!currentAuth) return;

    // Load room availability data
    await loadRoomAvailability();
});

/**
 * Fetch room availability from the API
 */
async function loadRoomAvailability() {
    const tbody = document.getElementById('roomTableBody');
    tbody.innerHTML = '<tr><td colspan="7" class="loading-spinner">⏳ Loading room data...</td></tr>';

    try {
        const response = await fetch('../api/rooms/availability', {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });

        if (!response.ok) {
            if (response.status === 401) {
                alert('Session expired. Please login again.');
                window.location.href = 'login.html';
                return;
            }
            throw new Error('HTTP ' + response.status);
        }

        const data = await response.json();

        if (!data.success) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#e74c3c;">❌ ' + (data.message || 'Failed to load rooms') + '</td></tr>';
            return;
        }

        // Store rooms for filtering
        allRooms = data.rooms || [];

        // Update summary cards
        document.getElementById('totalRooms').textContent = data.totalRooms || 0;
        document.getElementById('availableCount').textContent = data.availableRooms || 0;
        document.getElementById('occupiedCount').textContent = data.occupiedRooms || 0;

        // Render the table
        renderRoomTable(allRooms);

        console.log('Room availability loaded:', allRooms.length, 'rooms');

    } catch (error) {
        console.error('Error loading room availability:', error);
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#e74c3c;">❌ Network error. Please try again.</td></tr>';
    }
}

/**
 * Render the room table with given data
 */
function renderRoomTable(rooms) {
    const tbody = document.getElementById('roomTableBody');

    if (!rooms || rooms.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#7f8c8d;">No rooms found.</td></tr>';
        return;
    }

    // Check if user can add reservations (Admin/Manager only, not Staff)
    const canBook = currentAuth && currentAuth.role && hasPermission(currentAuth.role, 'canAddReservations');

    let html = '';
    rooms.forEach(room => {
        const isAvailable = room.status === 'Clean & Available';
        const statusClass = isAvailable ? 'available' : 'not-available';
        const statusIcon = isAvailable ? '✅' : '🔴';

        if (isAvailable && canBook) {
            html += '<tr class="room-row clickable" onclick="bookRoom(' + room.roomId + ')" title="Click to book Room #' + room.roomId + '">';
        } else if (!isAvailable) {
            html += '<tr class="room-row occupied">';
        } else {
            html += '<tr class="room-row">';
        }
        html += '<td><strong>' + room.roomId + '</strong>' + (isAvailable && canBook ? ' <span class="book-hint">📌 Click to book</span>' : '') + '</td>';
        html += '<td><span class="room-type-label">' + escapeHtml(room.roomType) + '</span></td>';
        html += '<td><span class="price">LKR ' + formatNumber(room.pricePerNight) + '</span></td>';
        html += '<td><span class="status-badge ' + statusClass + '">' + statusIcon + ' ' + escapeHtml(room.status) + '</span></td>';
        html += '<td>' + (room.checkInDate ? formatDate(room.checkInDate) : '-') + '</td>';
        html += '<td>' + (room.checkOutDate ? formatDate(room.checkOutDate) : '-') + '</td>';
        html += '<td>' + (room.guestName ? escapeHtml(room.guestName) : '<span style="color:#95a5a6;">-</span>') + '</td>';
        html += '</tr>';
    });

    tbody.innerHTML = html;
}

/**
 * Redirect to New Reservation page with pre-selected room
 */
function bookRoom(roomId) {
    window.location.href = 'add-reservation.html?roomId=' + roomId;
}

/**
 * Filter rooms based on dropdown selection
 */
function filterRooms() {
    const filter = document.getElementById('statusFilter').value;

    let filtered;
    if (filter === 'available') {
        filtered = allRooms.filter(r => r.status === 'Clean & Available');
    } else if (filter === 'occupied') {
        filtered = allRooms.filter(r => r.status === 'Not Available');
    } else {
        filtered = allRooms;
    }

    renderRoomTable(filtered);
}

/**
 * Format a date string
 */
function formatDate(dateStr) {
    if (!dateStr) return '-';
    try {
        const date = new Date(dateStr);
        return date.toLocaleDateString('en-GB', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    } catch (e) {
        return dateStr;
    }
}

/**
 * Format number with commas
 */
function formatNumber(num) {
    return parseFloat(num).toLocaleString('en-LK', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

/**
 * Escape HTML to prevent XSS
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
