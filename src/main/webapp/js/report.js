let currentAuth = null;

document.addEventListener('DOMContentLoaded', async function() {

    currentAuth = await initializePageAuth();
    if (!currentAuth) return;

    if (!hasPermission(currentAuth.role, 'canViewReports')) {
        const mainContent = document.querySelector('.main-content');
        if (mainContent) {
            mainContent.innerHTML = '<div class="alert alert-error" style="margin:40px;padding:20px;text-align:center;">' +
                '<h2>🚫 Access Denied</h2>' +
                '<p>You do not have permission to view reports. Only Admin and Manager roles can access this page.</p>' +
                '<p>Redirecting to Dashboard...</p></div>';
        }
        setTimeout(() => { window.location.href = 'dashboard.html'; }, 3000);
        return;
    }

    document.getElementById('reportForm').addEventListener('submit', function(e) {
        e.preventDefault();
        generateReport();
    });

    populatePrintMetadata();

    window.addEventListener('beforeprint', function() {
        populatePrintMetadata();
    });
});

function populatePrintMetadata() {
    const now = new Date();
    const printDate = document.getElementById('printDate');
    const printUser = document.getElementById('printUser');

    if (printDate) {
        printDate.textContent = now.toLocaleString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    if (printUser) {
        fetch('../api/login', { credentials: 'include' })
            .then(response => response.json())
            .then(data => {
                if (data.success && data.loggedIn) {
                    printUser.textContent = data.user.username;
                } else {
                    printUser.textContent = 'System User';
                }
            })
            .catch(() => {
                printUser.textContent = 'System User';
            });
    }
}

async function generateReport() {
    const reportType = document.getElementById('reportType').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!reportType || !startDate || !endDate) {
        alert('Please fill in all required fields');
        return;
    }

    if (new Date(endDate) < new Date(startDate)) {
        alert('End date must be after start date');
        return;
    }

    setReportLoading(true);

    try {
        switch (reportType) {
            case 'reservations':
                await fetchReservationReport(startDate, endDate);
                break;
            case 'revenue':
                await fetchRevenueReport(startDate, endDate);
                break;
            case 'occupancy':
                await fetchOccupancyReport();
                break;
            case 'guest':
                await fetchGuestReport(startDate, endDate);
                break;
            case 'payment':
                await fetchRevenueReport(startDate, endDate);
                break;
            default:
                alert('Invalid report type');
        }
    } catch (error) {
        console.error('Error generating report:', error);
        alert('Failed to generate report. Please try again.');
    } finally {
        setReportLoading(false);
    }
}

async function fetchReservationReport(startDate, endDate) {
    const response = await fetch(`../api/reports/date-range?startDate=${startDate}&endDate=${endDate}`, {
        credentials: 'include'
    });

    if (response.status === 401) {
        window.location.href = 'login.html';
        return;
    }

    const data = await response.json();
    console.log('Reservation report:', data);

    if (!data.success) {
        throw new Error(data.message || 'Failed to fetch report');
    }

    const report = data.report;

    document.getElementById('reportTotalReservations').textContent = report.totalReservations || 0;
    document.getElementById('reportTotalRevenue').textContent = formatLKR(report.totalRevenue || 0);
    document.getElementById('reportOccupancy').textContent = (report.occupancyRate || 0).toFixed(1) + '%';
    document.getElementById('reportCancellations').textContent = 'LKR ' + formatLKR(report.averageReservationValue || 0);

    updateCancellationLabel('Avg. Reservation Value');

    const thead = document.getElementById('reportTableHead');
    thead.innerHTML = '<tr><th>Metric</th><th>Value</th><th>Period</th></tr>';

    const tbody = document.getElementById('reportTableBody');
    tbody.innerHTML = `
        <tr>
            <td><strong>Total Reservations</strong></td>
            <td>${report.totalReservations || 0}</td>
            <td>${formatDate(startDate)} – ${formatDate(endDate)}</td>
        </tr>
        <tr>
            <td><strong>Total Revenue</strong></td>
            <td>${formatLKR(report.totalRevenue || 0)}</td>
            <td>${formatDate(startDate)} – ${formatDate(endDate)}</td>
        </tr>
        <tr>
            <td><strong>Average Reservation Value</strong></td>
            <td>${formatLKR(report.averageReservationValue || 0)}</td>
            <td>${formatDate(startDate)} – ${formatDate(endDate)}</td>
        </tr>
        <tr>
            <td><strong>Report Generated</strong></td>
            <td>${formatDate(report.generatedDate)}</td>
            <td>Type: ${report.reportType || 'FINANCIAL'}</td>
        </tr>
    `;
}

async function fetchRevenueReport(startDate, endDate) {
    const response = await fetch(`../api/reports/date-range?startDate=${startDate}&endDate=${endDate}`, {
        credentials: 'include'
    });

    if (response.status === 401) {
        window.location.href = 'login.html';
        return;
    }

    const data = await response.json();
    console.log('Revenue report:', data);

    if (!data.success) {
        throw new Error(data.message || 'Failed to fetch report');
    }

    const report = data.report;

    document.getElementById('reportTotalReservations').textContent = report.totalReservations || 0;
    document.getElementById('reportTotalRevenue').textContent = formatLKR(report.totalRevenue || 0);

    const avgPerDay = calculateAvgPerDay(report.totalRevenue || 0, startDate, endDate);
    document.getElementById('reportOccupancy').textContent = formatLKR(avgPerDay);
    document.getElementById('reportCancellations').textContent = formatLKR(report.averageReservationValue || 0);

    updateOccupancyLabel('Avg. Revenue/Day');
    updateCancellationLabel('Avg. per Reservation');

    const thead = document.getElementById('reportTableHead');
    thead.innerHTML = '<tr><th>Metric</th><th>Amount (LKR)</th><th>Details</th></tr>';

    const tbody = document.getElementById('reportTableBody');
    tbody.innerHTML = `
        <tr>
            <td><strong>Total Revenue</strong></td>
            <td>${formatLKR(report.totalRevenue || 0)}</td>
            <td>${formatDate(startDate)} – ${formatDate(endDate)}</td>
        </tr>
        <tr>
            <td><strong>Total Reservations</strong></td>
            <td>${report.totalReservations || 0}</td>
            <td>During selected period</td>
        </tr>
        <tr>
            <td><strong>Average per Reservation</strong></td>
            <td>${formatLKR(report.averageReservationValue || 0)}</td>
            <td>Per booking</td>
        </tr>
        <tr>
            <td><strong>Average Revenue per Day</strong></td>
            <td>${formatLKR(avgPerDay)}</td>
            <td>Daily average</td>
        </tr>
    `;
}

async function fetchOccupancyReport() {
    const response = await fetch('../api/reports/room-type?startDate=2020-01-01&endDate=2030-12-31', {
        credentials: 'include'
    });

    if (response.status === 401) {
        window.location.href = 'login.html';
        return;
    }

    const data = await response.json();
    console.log('Occupancy report:', data);

    const roomResponse = await fetch('../api/rooms/availability', { credentials: 'include' });
    const roomData = await roomResponse.json();
    console.log('Room availability:', roomData);

    if (roomData.success) {
        const total = roomData.totalRooms || 0;
        const occupied = roomData.occupiedRooms || 0;
        const available = roomData.availableRooms || 0;
        const occupancyRate = total > 0 ? ((occupied / total) * 100).toFixed(1) : 0;

        document.getElementById('reportTotalReservations').textContent = total;
        document.getElementById('reportTotalRevenue').textContent = available;
        document.getElementById('reportOccupancy').textContent = occupancyRate + '%';
        document.getElementById('reportCancellations').textContent = occupied;

        updateTotalReservationsLabel('Total Rooms');
        updateRevenueLabel('Available Rooms');
        updateOccupancyLabel('Occupancy Rate');
        updateCancellationLabel('Occupied Rooms');

        const thead = document.getElementById('reportTableHead');
        thead.innerHTML = '<tr><th>Room ID</th><th>Room Type</th><th>Price/Night (LKR)</th><th>Status</th><th>Guest</th></tr>';

        const tbody = document.getElementById('reportTableBody');
        if (roomData.rooms && roomData.rooms.length > 0) {
            tbody.innerHTML = roomData.rooms.map(room => `
                <tr>
                    <td><strong>${room.roomId}</strong></td>
                    <td>${escapeHtml(room.roomType)}</td>
                    <td>${formatNumber(room.pricePerNight)}</td>
                    <td><span class="status-badge status-${room.status === 'Clean & Available' ? 'confirmed' : 'cancelled'}">${escapeHtml(room.status)}</span></td>
                    <td>${room.guestName ? escapeHtml(room.guestName) : '-'}</td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="5" class="no-data">No room data available</td></tr>';
        }
    }
}

async function fetchGuestReport(startDate, endDate) {

    const [reportResponse, reservationsResponse] = await Promise.all([
        fetch(`../api/reports/date-range?startDate=${startDate}&endDate=${endDate}`, { credentials: 'include' }),
        fetch('../api/reservations?filter=all', { credentials: 'include' })
    ]);

    if (reportResponse.status === 401) {
        window.location.href = 'login.html';
        return;
    }

    const reportData = await reportResponse.json();
    const reservationsData = await reservationsResponse.json();
    console.log('Guest report data:', reportData, reservationsData);

    const report = reportData.success ? reportData.report : {};

    let reservations = [];
    if (reservationsData.success) {
        reservations = reservationsData.reservations || reservationsData || [];
    } else if (Array.isArray(reservationsData)) {
        reservations = reservationsData;
    }

    const start = new Date(startDate);
    const end = new Date(endDate);

    const filteredReservations = reservations.filter(r => {
        const checkIn = new Date(r.checkInDate);
        return checkIn >= start && checkIn <= end;
    });

    const guestMap = {};
    filteredReservations.forEach(r => {
        const name = r.guestName || 'Unknown';
        if (!guestMap[name]) {
            guestMap[name] = { count: 0, totalSpent: 0 };
        }
        guestMap[name].count++;
        guestMap[name].totalSpent += parseFloat(r.totalAmount || 0);
    });

    const guestList = Object.entries(guestMap)
        .map(([name, data]) => ({ name, ...data }))
        .sort((a, b) => b.totalSpent - a.totalSpent);

    document.getElementById('reportTotalReservations').textContent = filteredReservations.length;
    document.getElementById('reportTotalRevenue').textContent = formatLKR(report.totalRevenue || 0);
    document.getElementById('reportOccupancy').textContent = guestList.length;
    document.getElementById('reportCancellations').textContent = filteredReservations.length > 0
        ? formatLKR((report.totalRevenue || 0) / guestList.length) : 'LKR 0.00';

    updateOccupancyLabel('Unique Guests');
    updateCancellationLabel('Avg. per Guest');

    const thead = document.getElementById('reportTableHead');
    thead.innerHTML = '<tr><th>#</th><th>Guest Name</th><th>Reservations</th><th>Total Spent (LKR)</th></tr>';

    const tbody = document.getElementById('reportTableBody');
    if (guestList.length > 0) {
        tbody.innerHTML = guestList.map((guest, i) => `
            <tr>
                <td>${i + 1}</td>
                <td><strong>${escapeHtml(guest.name)}</strong></td>
                <td>${guest.count}</td>
                <td>${formatLKR(guest.totalSpent)}</td>
            </tr>
        `).join('');
    } else {
        tbody.innerHTML = '<tr><td colspan="4" class="no-data">No guest data found for selected period</td></tr>';
    }
}

async function generateQuickReport(period) {
    const endDate = new Date();
    let startDate = new Date();

    switch(period) {
        case 'today':
            startDate = new Date();
            break;
        case 'week':
            startDate.setDate(startDate.getDate() - 7);
            break;
        case 'month':
            startDate.setMonth(startDate.getMonth() - 1);
            break;
        case 'year':
            startDate.setFullYear(startDate.getFullYear() - 1);
            break;
    }

    document.getElementById('startDate').value = startDate.toISOString().split('T')[0];
    document.getElementById('endDate').value = endDate.toISOString().split('T')[0];
    document.getElementById('reportType').value = 'reservations';

    await generateReport();
}

function exportReport() {
    window.print();
}

function formatLKR(amount) {
    const num = parseFloat(amount || 0);
    return 'LKR ' + new Intl.NumberFormat('en-LK', {
        style: 'decimal',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(num);
}

function formatNumber(num) {
    return new Intl.NumberFormat('en-LK', {
        style: 'decimal',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(num || 0);
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function calculateAvgPerDay(totalRevenue, startDate, endDate) {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const days = Math.max(1, Math.ceil((end - start) / (1000 * 60 * 60 * 24)));
    return parseFloat(totalRevenue || 0) / days;
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function setReportLoading(loading) {
    if (loading) {
        document.getElementById('reportTotalReservations').textContent = '...';
        document.getElementById('reportTotalRevenue').textContent = '...';
        document.getElementById('reportOccupancy').textContent = '...';
        document.getElementById('reportCancellations').textContent = '...';
        document.getElementById('reportTableBody').innerHTML = '<tr><td colspan="5" class="no-data">Loading report data...</td></tr>';
    }

    resetCardLabels();
}

function resetCardLabels() {
    const cards = document.querySelectorAll('.stats-summary .stat-card h3');
    if (cards.length >= 4) {
        cards[0].textContent = 'Total Reservations';
        cards[1].textContent = 'Total Revenue';
        cards[2].textContent = 'Average Occupancy';
        cards[3].textContent = 'Cancellation Rate';
    }
}

function updateTotalReservationsLabel(label) {
    const cards = document.querySelectorAll('.stats-summary .stat-card h3');
    if (cards.length >= 1) cards[0].textContent = label;
}

function updateRevenueLabel(label) {
    const cards = document.querySelectorAll('.stats-summary .stat-card h3');
    if (cards.length >= 2) cards[1].textContent = label;
}

function updateOccupancyLabel(label) {
    const cards = document.querySelectorAll('.stats-summary .stat-card h3');
    if (cards.length >= 3) cards[2].textContent = label;
}

function updateCancellationLabel(label) {
    const cards = document.querySelectorAll('.stats-summary .stat-card h3');
    if (cards.length >= 4) cards[3].textContent = label;
}
