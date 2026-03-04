let currentAuth = null;

document.addEventListener('DOMContentLoaded', async function() {

    currentAuth = await initializePageAuth();
    if (!currentAuth) return;

    if (!hasPermission(currentAuth.role, 'canPrintInvoice')) {
        alert('You do not have permission to view invoices.');
        window.location.href = 'dashboard.html';
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const reservationId = urlParams.get('id');

    if (reservationId) {
        await loadInvoiceData(reservationId);
    } else {
        showError('No reservation ID provided. Please go back and select a reservation.');
    }

    window.addEventListener('beforeprint', function() {
        console.log('Preparing invoice for printing...');
    });

    window.addEventListener('afterprint', function() {
        console.log('Print dialog closed');
    });
});

async function loadInvoiceData(reservationId) {
    console.log('Loading invoice for reservation ID:', reservationId);

    try {
        const response = await fetch(`../api/invoice/${reservationId}`, {
            method: 'GET',
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                alert('Session expired. Please login again.');
                window.location.href = 'login.html';
                return;
            }
            const errorData = await response.json().catch(() => null);
            const errorMsg = errorData?.message || `Failed to load invoice (HTTP ${response.status})`;
            showError(errorMsg);
            return;
        }

        const data = await response.json();

        if (!data.success || !data.invoice) {
            showError(data.message || 'Failed to load invoice data.');
            return;
        }

        populateInvoice(data.invoice);
        console.log('Invoice loaded successfully for reservation:', reservationId);

    } catch (error) {
        console.error('Error loading invoice:', error);
        showError('Network error while loading invoice. Please try again.');
    }
}

function populateInvoice(invoice) {

    document.getElementById('invoiceNumber').textContent = 'INV-' + invoice.reservationNumber;
    document.getElementById('invoiceDate').textContent = formatDate(invoice.invoiceDate);

    document.getElementById('guestName').textContent = invoice.guestName || '-';
    document.getElementById('guestEmail').textContent = invoice.guestAddress || '-';
    document.getElementById('guestPhone').textContent = invoice.guestContact || '-';
    document.getElementById('guestId').textContent = invoice.reservationNumber || '-';

    document.getElementById('reservationId').textContent = invoice.reservationNumber || '-';
    document.getElementById('checkIn').textContent = formatDate(invoice.checkInDate);
    document.getElementById('checkOut').textContent = formatDate(invoice.checkOutDate);
    document.getElementById('roomType').textContent = invoice.roomType || '-';
    document.getElementById('nights').textContent = invoice.numberOfNights || 0;
    document.getElementById('guests').textContent = '-';

    const nights = invoice.numberOfNights || 0;
    const rate = invoice.pricePerNight || 0;
    const charges = invoice.charges || {};

    document.getElementById('roomNights').textContent = nights;
    document.getElementById('roomRate').textContent = formatCurrency(rate);
    document.getElementById('roomTotal').textContent = formatCurrency(charges.roomCharges || 0);
    document.getElementById('subtotal').textContent = formatCurrency(charges.roomCharges || 0);
    document.getElementById('tax').textContent = formatCurrency(charges.tax || 0);
    document.getElementById('advance').textContent = formatCurrency(charges.serviceCharge || 0);
    document.getElementById('totalDue').textContent = formatCurrency(charges.totalAmount || 0);

    document.getElementById('paymentMethod').textContent = invoice.paymentMethod || '-';
    document.getElementById('paymentStatus').textContent = invoice.paymentStatus || 'Pending';

    const statusEl = document.getElementById('paymentStatus');
    if (invoice.paymentStatus === 'Paid') {
        statusEl.style.color = '#27ae60';
        statusEl.style.fontWeight = 'bold';
    }
}

function formatCurrency(amount) {
    return 'LKR ' + parseFloat(amount).toLocaleString('en-LK', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    try {
        const date = new Date(dateStr);
        return date.toLocaleDateString('en-GB', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    } catch (e) {
        return dateStr;
    }
}

function showError(message) {
    const container = document.querySelector('.invoice-body');
    if (container) {
        container.innerHTML = `
            <div style="text-align: center; padding: 60px 20px;">
                <h2 style="color: #e74c3c;">⚠️ Error</h2>
                <p style="font-size: 16px; color: #555;">${message}</p>
                <a href="view-reservation.html" class="btn-primary" style="display: inline-block; margin-top: 20px; text-decoration: none;">
                    ⬅️ Back to Reservations
                </a>
            </div>
        `;
    }
}

function downloadPDF() {
    window.print();
}
