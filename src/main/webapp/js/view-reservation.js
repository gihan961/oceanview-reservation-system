// View Reservation Page JavaScript
// Handles reservation listing, search, filter, view details, and delete operations

let allReservations = [];
let filteredReservations = [];
let currentPage = 1;
const itemsPerPage = 10;
let reservationToDelete = null;
let userRole = null; // Store user role globally

document.addEventListener('DOMContentLoaded', async function() {
    console.log('View Reservation page loading...');
    
    // Check authentication and permissions
    try {
        const auth = await initializePageAuth();
        if (!auth) {
            console.log('Not authenticated, redirecting to login');
            return; // initializePageAuth already redirects
        }

        console.log('Auth successful, role:', auth.role);

        // Store user role globally
        userRole = auth.role;
        
        if (!hasPermission(auth.role, 'canViewReservations')) {
            showError('You do not have permission to view reservations.');
            document.querySelector('.reservations-section').style.display = 'none';
            return;
        }

        console.log('Permission check passed, initializing page...');
        initializePage();
    } catch (error) {
        console.error('Auth check failed:', error);
        window.location.href = 'login.html';
    }
});

/**
 * Initialize the page
 */
function initializePage() {
    console.log('initializePage() called');
    
    // Load all reservations FIRST (most important)
    loadReservations();
    
    // Set up event listeners with error handling
    try {
        // Search form handler
        const searchForm = document.getElementById('searchForm');
        if (searchForm) {
            searchForm.addEventListener('submit', function(e) {
                e.preventDefault();
                applyFilters();
            });
        }
        
        // Reset button handler
        const resetBtn = document.getElementById('resetBtn');
        if (resetBtn) {
            resetBtn.addEventListener('click', function() {
                document.getElementById('searchForm').reset();
                filteredReservations = [...allReservations];
                currentPage = 1;
                renderTable();
                updatePagination();
            });
        }
        
        // Refresh button handler
        const refreshBtn = document.getElementById('refreshBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', function() {
                loadReservations();
            });
        }
        
        // Pagination handlers
        const prevPage = document.getElementById('prevPage');
        if (prevPage) {
            prevPage.addEventListener('click', function() {
                if (currentPage > 1) {
                    currentPage--;
                    renderTable();
                    updatePagination();
                }
            });
        }
        
        const nextPage = document.getElementById('nextPage');
        if (nextPage) {
            nextPage.addEventListener('click', function() {
                const totalPages = Math.ceil(filteredReservations.length / itemsPerPage);
                if (currentPage < totalPages) {
                    currentPage++;
                    renderTable();
                    updatePagination();
                }
            });
        }
        
        // Modal close handlers
        const closeModal = document.getElementById('closeModal');
        if (closeModal) {
            closeModal.addEventListener('click', function() {
                document.getElementById('detailsModal').style.display = 'none';
            });
        }
        
        const cancelDelete = document.getElementById('cancelDelete');
        if (cancelDelete) {
            cancelDelete.addEventListener('click', function() {
                document.getElementById('deleteModal').style.display = 'none';
                reservationToDelete = null;
            });
        }
        
        const confirmDelete = document.getElementById('confirmDelete');
        if (confirmDelete) {
            confirmDelete.addEventListener('click', function() {
                if (reservationToDelete) {
                    deleteReservation(reservationToDelete);
                }
            });
        }
        
        // Click outside modal to close
        window.addEventListener('click', function(event) {
            const detailsModal = document.getElementById('detailsModal');
            const deleteModal = document.getElementById('deleteModal');
            if (event.target === detailsModal) {
                detailsModal.style.display = 'none';
            }
            if (event.target === deleteModal) {
                deleteModal.style.display = 'none';
                reservationToDelete = null;
            }
        });
        
        console.log('All event listeners attached successfully');
    } catch (error) {
        console.error('Error setting up event listeners:', error);
    }
}

/**
 * Load all reservations from the API
 */
async function loadReservations() {
    const loadingSpinner = document.getElementById('loadingSpinner');
    const tbody = document.getElementById('reservationsTableBody');
    
    try {
        console.log('=== Loading Reservations ===');
        if (loadingSpinner) loadingSpinner.style.display = 'block';
        if (tbody) tbody.innerHTML = '<tr><td colspan="9" class="no-data">Loading...</td></tr>';
        
        const fetchUrl = '../api/reservations';
        console.log('Fetching from:', fetchUrl);
        
        const response = await fetch(fetchUrl, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        console.log('Response status:', response.status);
        
        if (response.status === 401) {
            console.log('Unauthorized - redirecting to login');
            window.location.href = 'login.html';
            return;
        }
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('API error response:', errorText);
            throw new Error(`HTTP error! status: ${response.status}, body: ${errorText}`);
        }
        
        const responseText = await response.text();
        console.log('Raw API response:', responseText);
        
        let data;
        try {
            data = JSON.parse(responseText);
        } catch (parseError) {
            console.error('JSON parse error:', parseError);
            throw new Error('Invalid JSON response from server');
        }
        
        console.log('Parsed data:', data);
        
        // Extract reservations array from response
        let reservations = [];
        if (data.reservations && Array.isArray(data.reservations)) {
            reservations = data.reservations;
        } else if (Array.isArray(data)) {
            reservations = data;
        }
        
        console.log('Reservations count:', reservations.length);
        
        allReservations = reservations;
        filteredReservations = [...reservations];
        currentPage = 1;
        
        renderTable();
        updatePagination();
        updateTotalCount();
        
        console.log('=== Reservations loaded successfully ===');
        
    } catch (error) {
        console.error('Error loading reservations:', error);
        showError('Failed to load reservations: ' + error.message);
        if (tbody) tbody.innerHTML = '<tr><td colspan="9" class="no-data error-text">Error loading reservations. Please try refreshing.</td></tr>';
        
    } finally {
        if (loadingSpinner) loadingSpinner.style.display = 'none';
    }
}

/**
 * Apply search filters
 */
function applyFilters() {
    const reservationNumber = document.getElementById('searchReservationNumber').value.trim().toLowerCase();
    const guestName = document.getElementById('searchGuestName').value.trim().toLowerCase();
    const roomId = document.getElementById('searchRoomId').value.trim();
    const checkInDate = document.getElementById('searchCheckInDate').value;
    const checkOutDate = document.getElementById('searchCheckOutDate').value;
    
    filteredReservations = allReservations.filter(reservation => {
        let matches = true;
        
        if (reservationNumber && !reservation.reservationNumber.toLowerCase().includes(reservationNumber)) {
            matches = false;
        }
        
        if (guestName && !reservation.guestName.toLowerCase().includes(guestName)) {
            matches = false;
        }
        
        if (roomId && reservation.roomId.toString() !== roomId) {
            matches = false;
        }
        
        if (checkInDate && reservation.checkInDate < checkInDate) {
            matches = false;
        }
        
        if (checkOutDate && reservation.checkOutDate > checkOutDate) {
            matches = false;
        }
        
        return matches;
    });
    
    currentPage = 1;
    renderTable();
    updatePagination();
    updateTotalCount();
    
    if (filteredReservations.length === 0) {
        showWarning('No reservations found matching your search criteria.');
    }
}

/**
 * Render the reservations table
 */
function renderTable() {
    const tbody = document.getElementById('reservationsTableBody');
    
    if (filteredReservations.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="no-data">No reservations found</td></tr>';
        return;
    }
    
    // Calculate pagination
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = Math.min(startIndex + itemsPerPage, filteredReservations.length);
    const pageReservations = filteredReservations.slice(startIndex, endIndex);
    
    // Render rows
    tbody.innerHTML = pageReservations.map(reservation => {
        const checkInDate = new Date(reservation.checkInDate);
        const checkOutDate = new Date(reservation.checkOutDate);
        const nights = Math.ceil((checkOutDate - checkInDate) / (1000 * 60 * 60 * 24));
        
        // Build action buttons based on role
        let actionButtons = `<button onclick="viewDetails(${reservation.id})" class="btn-icon" title="View Details">👁️</button>`;
        actionButtons += `<button onclick="viewInvoice(${reservation.id})" class="btn-icon" title="View Invoice" data-permission="canPrintInvoice">🧾</button>`;
        
        // Only Admin and Manager can delete/cancel reservations
        if (userRole && hasPermission(userRole, 'canDeleteReservations')) {
            actionButtons += `<button onclick="confirmDelete(${reservation.id})" class="btn-icon btn-danger" title="Delete">🗑️</button>`;
        }
        
        return `
            <tr>
                <td><strong>${reservation.reservationNumber || 'N/A'}</strong></td>
                <td>${reservation.guestName}</td>
                <td>
                    <div class="contact-info">
                        <div>📞 ${reservation.contactNumber || 'N/A'}</div>
                        <div class="text-muted small">${reservation.address ? reservation.address.substring(0, 30) + '...' : 'N/A'}</div>
                    </div>
                </td>
                <td><span class="room-badge">Room ${reservation.roomId}</span></td>
                <td>${formatDate(reservation.checkInDate)}</td>
                <td>${formatDate(reservation.checkOutDate)}</td>
                <td>${nights} night${nights !== 1 ? 's' : ''}</td>
                <td><strong class="amount-text">LKR ${parseFloat(reservation.totalAmount).toLocaleString('en-LK', {minimumFractionDigits: 2, maximumFractionDigits: 2})}</strong></td>
                <td>
                    <div class="action-buttons">
                        ${actionButtons}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
    
    // Apply RBAC to action buttons
    if (userRole) {
        applyRoleBasedUI(userRole);
    }
}

/**
 * Update pagination controls
 */
function updatePagination() {
    const totalPages = Math.ceil(filteredReservations.length / itemsPerPage);
    const paginationControls = document.getElementById('paginationControls');
    const prevBtn = document.getElementById('prevPage');
    const nextBtn = document.getElementById('nextPage');
    const pageInfo = document.getElementById('pageInfo');
    
    if (totalPages <= 1) {
        paginationControls.style.display = 'none';
        return;
    }
    
    paginationControls.style.display = 'flex';
    pageInfo.textContent = `Page ${currentPage} of ${totalPages}`;
    
    prevBtn.disabled = currentPage === 1;
    nextBtn.disabled = currentPage === totalPages;
}

/**
 * Update total count badge
 */
function updateTotalCount() {
    const totalCount = document.getElementById('totalCount');
    totalCount.textContent = `${filteredReservations.length} reservation${filteredReservations.length !== 1 ? 's' : ''}`;
}

/**
 * View reservation details in modal
 */
async function viewDetails(reservationId) {
    const modal = document.getElementById('detailsModal');
    const detailsContent = document.getElementById('reservationDetails');
    
    try {
        detailsContent.innerHTML = '<p>Loading details...</p>';
        modal.style.display = 'block';
        
        const response = await fetch(`../api/reservations/${reservationId}`, {
            method: 'GET',
            credentials: 'include'
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const reservation = await response.json();
        
        const checkInDate = new Date(reservation.checkInDate);
        const checkOutDate = new Date(reservation.checkOutDate);
        const nights = Math.ceil((checkOutDate - checkInDate) / (1000 * 60 * 60 * 24));
        
        detailsContent.innerHTML = `
            <div class="details-grid">
                <div class="detail-item">
                    <label>Reservation Number:</label>
                    <span class="detail-value"><strong>${reservation.reservationNumber || 'N/A'}</strong></span>
                </div>
                <div class="detail-item">
                    <label>Guest Name:</label>
                    <span class="detail-value">${reservation.guestName}</span>
                </div>
                <div class="detail-item">
                    <label>Contact Number:</label>
                    <span class="detail-value">${reservation.contactNumber || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <label>Address:</label>
                    <span class="detail-value">${reservation.address || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <label>Room ID:</label>
                    <span class="detail-value">Room ${reservation.roomId}</span>
                </div>
                <div class="detail-item">
                    <label>Room Type:</label>
                    <span class="detail-value">${reservation.roomType || 'N/A'}</span>
                </div>
                <div class="detail-item">
                    <label>Check-In Date:</label>
                    <span class="detail-value">${formatDate(reservation.checkInDate)}</span>
                </div>
                <div class="detail-item">
                    <label>Check-Out Date:</label>
                    <span class="detail-value">${formatDate(reservation.checkOutDate)}</span>
                </div>
                <div class="detail-item">
                    <label>Total Nights:</label>
                    <span class="detail-value">${nights} night${nights !== 1 ? 's' : ''}</span>
                </div>
                <div class="detail-item">
                    <label>Price Per Night:</label>
                    <span class="detail-value">$${parseFloat(reservation.pricePerNight || 0).toFixed(2)}</span>
                </div>
                <div class="detail-item highlight">
                    <label>Total Amount:</label>
                    <span class="detail-value"><strong class="amount-text">$${parseFloat(reservation.totalAmount).toFixed(2)}</strong></span>
                </div>
            </div>
            <div class="modal-footer">
                <button onclick="viewInvoice(${reservation.id})" class="btn-primary">View Invoice</button>
                <button onclick="document.getElementById('detailsModal').style.display='none'" class="btn-secondary">Close</button>
            </div>
        `;
        
    } catch (error) {
        console.error('Error loading reservation details:', error);
        detailsContent.innerHTML = '<p class="error-text">Failed to load reservation details.</p>';
    }
}

/**
 * Confirm delete reservation
 */
function confirmDelete(reservationId) {
    reservationToDelete = reservationId;
    document.getElementById('deleteModal').style.display = 'block';
}

/**
 * Delete reservation
 */
async function deleteReservation(reservationId) {
    const deleteModal = document.getElementById('deleteModal');
    
    try {
        const response = await fetch(`../api/reservations/${reservationId}`, {
            method: 'DELETE',
            credentials: 'include'
        });
        
        if (response.status === 401) {
            window.location.href = '../index.html';
            return;
        }
        
        if (response.status === 403) {
            showError('You do not have permission to delete reservations.');
            deleteModal.style.display = 'none';
            return;
        }
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        showSuccess('Reservation deleted successfully!');
        deleteModal.style.display = 'none';
        reservationToDelete = null;
        
        // Reload reservations
        loadReservations();
        
    } catch (error) {
        console.error('Error deleting reservation:', error);
        showError('Failed to delete reservation. Please try again.');
        deleteModal.style.display = 'none';
    }
}

/**
 * View invoice for reservation
 */
function viewInvoice(reservationId) {
    window.location.href = `invoice.html?id=${reservationId}`;
}

/**
 * Format date to readable string
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric' 
    });
}

/**
 * Show success message
 */
function showSuccess(message) {
    const messageArea = document.getElementById('messageArea');
    messageArea.className = 'message-area success';
    messageArea.textContent = message;
    messageArea.style.display = 'block';
    window.scrollTo({ top: 0, behavior: 'smooth' });
    
    setTimeout(() => {
        messageArea.style.display = 'none';
    }, 5000);
}

/**
 * Show error message
 */
function showError(message) {
    const messageArea = document.getElementById('messageArea');
    messageArea.className = 'message-area error';
    messageArea.textContent = message;
    messageArea.style.display = 'block';
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

/**
 * Show warning message
 */
function showWarning(message) {
    const messageArea = document.getElementById('messageArea');
    messageArea.className = 'message-area warning';
    messageArea.textContent = message;
    messageArea.style.display = 'block';
    window.scrollTo({ top: 0, behavior: 'smooth' });
    
    setTimeout(() => {
        messageArea.style.display = 'none';
    }, 5000);
}

/**
 * Logout handler
 */
function logout(event) {
    event.preventDefault();
    
    fetch('../api/logout', {
        method: 'POST',
        credentials: 'include'
    }).then(() => {
        sessionStorage.clear();
        window.location.href = '../index.html';
    }).catch(error => {
        console.error('Logout error:', error);
        sessionStorage.clear();
        window.location.href = '../index.html';
    });
}
