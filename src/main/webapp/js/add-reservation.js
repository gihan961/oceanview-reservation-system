let availableRooms = [];
let selectedRoom = null;

document.addEventListener('DOMContentLoaded', async function() {

    try {
        const auth = await initializePageAuth();
        if (!auth) {
            return;
        }

        if (!hasPermission(auth.role, 'canAddReservations')) {
            showError('Access Denied — You do not have permission to add reservations. Staff accounts are view-only.');
            document.getElementById('addReservationForm').style.display = 'none';

            setTimeout(() => { window.location.href = 'dashboard.html'; }, 3000);
            return;
        }

        initializePage();
    } catch (error) {
        console.error('Auth check failed:', error);
        window.location.href = 'login.html';
    }
});

function initializePage() {
    const form = document.getElementById('addReservationForm');
    const checkInInput = document.getElementById('checkInDate');
    const checkOutInput = document.getElementById('checkOutDate');
    const roomSelect = document.getElementById('roomId');

    const today = new Date().toISOString().split('T')[0];
    checkInInput.min = today;
    checkOutInput.min = today;

    loadAvailableRooms();

    checkInInput.addEventListener('change', function() {
        const checkInDate = new Date(this.value);
        const nextDay = new Date(checkInDate);
        nextDay.setDate(nextDay.getDate() + 1);
        checkOutInput.min = nextDay.toISOString().split('T')[0];

        if (checkOutInput.value && checkOutInput.value <= this.value) {
            checkOutInput.value = '';
        }

        calculateTotal();
    });

    checkOutInput.addEventListener('change', function() {
        calculateTotal();
    });

    roomSelect.addEventListener('change', function() {
        const roomId = parseInt(this.value);
        selectedRoom = availableRooms.find(room => room.id === roomId);

        if (selectedRoom) {
            const formattedPrice = new Intl.NumberFormat('en-LK', {
                style: 'decimal',
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }).format(selectedRoom.pricePerNight);
            document.getElementById('pricePerNight').textContent = formattedPrice;
            calculateTotal();
        } else {
            document.getElementById('pricePerNight').textContent = '0.00';
            document.getElementById('totalNights').textContent = '0';
            document.getElementById('totalAmount').textContent = '0.00';
        }
    });

    form.addEventListener('submit', function(e) {
        e.preventDefault();

        if (validateForm()) {
            submitReservation();
        }
    });

    const inputs = form.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            clearFieldError(this.id);
        });
    });

    document.getElementById('resetBtn').addEventListener('click', function() {
        clearAllErrors();
        document.getElementById('totalNights').textContent = '0';
        document.getElementById('pricePerNight').textContent = '0.00';
        document.getElementById('totalAmount').textContent = '0.00';
        selectedRoom = null;
    });
}

async function loadAvailableRooms() {
    const roomSelect = document.getElementById('roomId');

    try {
        roomSelect.innerHTML = '<option value="">Loading available rooms...</option>';

        const response = await fetch('../api/rooms?status=AVAILABLE', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.status === 401) {
            window.location.href = '../index.html';
            return;
        }

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log('Rooms API response:', data);

        const rooms = data.rooms || data || [];
        availableRooms = rooms;

        if (rooms.length === 0) {
            roomSelect.innerHTML = '<option value="">No rooms available</option>';
            showWarning('No rooms are currently available. Please check back later.');
            document.getElementById('submitBtn').disabled = true;
            return;
        }

        roomSelect.innerHTML = '<option value="">-- Select a Room --</option>';
        rooms.forEach(room => {
            const option = document.createElement('option');
            option.value = room.id;

            const formattedPrice = new Intl.NumberFormat('en-LK', {
                style: 'decimal',
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }).format(room.pricePerNight);
            option.textContent = `Room #${room.id} - ${room.roomType} (LKR ${formattedPrice}/night)`;
            option.dataset.price = room.pricePerNight;
            option.dataset.type = room.roomType;
            roomSelect.appendChild(option);
        });

        autoSelectRoomFromURL(roomSelect);

    } catch (error) {
        console.error('Error loading rooms:', error);
        roomSelect.innerHTML = '<option value="">Error loading rooms</option>';
        showError('Failed to load available rooms. Please refresh the page.');
    }
}

function autoSelectRoomFromURL(roomSelect) {
    const urlParams = new URLSearchParams(window.location.search);
    const preselectedRoomId = urlParams.get('roomId');

    if (!preselectedRoomId) return;

    const matchingOption = Array.from(roomSelect.options).find(opt => opt.value === preselectedRoomId);

    if (matchingOption) {
        roomSelect.value = preselectedRoomId;

        roomSelect.dispatchEvent(new Event('change'));

        showPreselectedBanner(preselectedRoomId, matchingOption.textContent);
        console.log('Room #' + preselectedRoomId + ' auto-selected from Available Rooms page');
    } else {
        showWarning('Room #' + preselectedRoomId + ' is no longer available. Please select another room.');
    }

    const cleanURL = window.location.pathname;
    window.history.replaceState({}, document.title, cleanURL);
}

function showPreselectedBanner(roomId, roomLabel) {

    const existing = document.getElementById('preselectedBanner');
    if (existing) existing.remove();

    const banner = document.createElement('div');
    banner.id = 'preselectedBanner';
    banner.style.cssText = 'background: linear-gradient(135deg, #d4edda, #c3e6cb); border: 1px solid #28a745; ' +
        'border-radius: 8px; padding: 12px 18px; margin-bottom: 20px; display: flex; align-items: center; ' +
        'gap: 10px; font-size: 14px; color: #155724; animation: fadeIn 0.3s ease;';
    banner.innerHTML = '<span style="font-size:20px;">🛏️</span>' +
        '<div><strong>Room Pre-Selected</strong><br>' +
        '<span style="font-size:13px;">' + escapeHtmlLocal(roomLabel) + ' — selected from Available Rooms. You can change it below.</span></div>' +
        '<button onclick="this.parentElement.remove()" style="margin-left:auto; background:none; border:none; ' +
        'font-size:18px; cursor:pointer; color:#155724; padding:0 4px;">✕</button>';

    const form = document.getElementById('addReservationForm');
    form.parentNode.insertBefore(banner, form);
}

function escapeHtmlLocal(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function calculateTotal() {
    const checkInDate = document.getElementById('checkInDate').value;
    const checkOutDate = document.getElementById('checkOutDate').value;

    if (!checkInDate || !checkOutDate || !selectedRoom) {
        return;
    }

    const checkIn = new Date(checkInDate);
    const checkOut = new Date(checkOutDate);

    if (checkOut <= checkIn) {
        document.getElementById('totalNights').textContent = '0';
        document.getElementById('totalAmount').textContent = '0.00';
        return;
    }

    const diffTime = checkOut - checkIn;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    const totalAmount = diffDays * selectedRoom.pricePerNight;

    const formattedTotal = new Intl.NumberFormat('en-LK', {
        style: 'decimal',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(totalAmount);

    document.getElementById('totalNights').textContent = diffDays;
    document.getElementById('totalAmount').textContent = formattedTotal;
}

function validateForm() {
    let isValid = true;
    clearAllErrors();

    const guestName = document.getElementById('guestName').value.trim();
    if (guestName.length < 2) {
        showFieldError('guestName', 'Guest name must be at least 2 characters');
        isValid = false;
    }

    const contactNumber = document.getElementById('contactNumber').value.trim();
    if (contactNumber.length < 10) {
        showFieldError('contactNumber', 'Please enter a valid phone number');
        isValid = false;
    }

    const address = document.getElementById('address').value.trim();
    if (address.length < 5) {
        showFieldError('address', 'Please enter a complete address');
        isValid = false;
    }

    const checkInDate = document.getElementById('checkInDate').value;
    const checkOutDate = document.getElementById('checkOutDate').value;

    if (!checkInDate) {
        showFieldError('checkInDate', 'Check-in date is required');
        isValid = false;
    } else {
        const checkIn = new Date(checkInDate);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (checkIn < today) {
            showFieldError('checkInDate', 'Check-in date cannot be in the past');
            isValid = false;
        }
    }

    if (!checkOutDate) {
        showFieldError('checkOutDate', 'Check-out date is required');
        isValid = false;
    } else if (checkInDate && checkOutDate) {
        const checkIn = new Date(checkInDate);
        const checkOut = new Date(checkOutDate);

        if (checkOut <= checkIn) {
            showFieldError('checkOutDate', 'Check-out must be after check-in date');
            isValid = false;
        }
    }

    const roomId = document.getElementById('roomId').value;
    if (!roomId) {
        showFieldError('roomId', 'Please select a room');
        isValid = false;
    }

    return isValid;
}

async function submitReservation() {
    const submitBtn = document.getElementById('submitBtn');
    const submitBtnText = document.getElementById('submitBtnText');
    const submitBtnLoading = document.getElementById('submitBtnLoading');

    submitBtn.disabled = true;
    submitBtnText.style.display = 'none';
    submitBtnLoading.style.display = 'inline';

    try {

        const formData = {
            guestName: document.getElementById('guestName').value.trim(),
            address: document.getElementById('address').value.trim(),
            contactNumber: document.getElementById('contactNumber').value.trim(),
            roomId: parseInt(document.getElementById('roomId').value),
            checkInDate: document.getElementById('checkInDate').value,
            checkOutDate: document.getElementById('checkOutDate').value
        };

        console.log('Submitting reservation:', formData);

        const response = await fetch('../api/reservations', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(formData)
        });

        if (response.status === 401) {
            window.location.href = '../index.html';
            return;
        }

        if (response.status === 403) {
            showError('You do not have permission to create reservations.');
            return;
        }

        const responseText = await response.text();
        let result;

        try {
            result = JSON.parse(responseText);
        } catch (e) {
            result = { message: responseText };
        }

        if (response.ok) {
            showSuccess(`Reservation created successfully! Reservation Number: ${result.reservationNumber || 'N/A'}`);

            setTimeout(() => {
                window.location.href = 'view-reservation.html';
            }, 2000);

        } else {

            if (result.errors && Array.isArray(result.errors)) {
                result.errors.forEach(error => {
                    if (error.field) {
                        showFieldError(error.field, error.message);
                    }
                });
                showError('Please correct the errors and try again.');
            } else {
                showError(result.message || 'Failed to create reservation. Please try again.');
            }
        }

    } catch (error) {
        console.error('Error submitting reservation:', error);
        showError('An error occurred while creating the reservation. Please try again.');

    } finally {

        submitBtn.disabled = false;
        submitBtnText.style.display = 'inline';
        submitBtnLoading.style.display = 'none';
    }
}

function showSuccess(message) {
    const messageArea = document.getElementById('messageArea');
    messageArea.className = 'message-area success';
    messageArea.textContent = message;
    messageArea.style.display = 'block';

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function showError(message) {
    const messageArea = document.getElementById('messageArea');
    messageArea.className = 'message-area error';
    messageArea.textContent = message;
    messageArea.style.display = 'block';

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function showWarning(message) {
    const messageArea = document.getElementById('messageArea');
    messageArea.className = 'message-area warning';
    messageArea.textContent = message;
    messageArea.style.display = 'block';
}

function showFieldError(fieldId, message) {
    const errorElement = document.getElementById(fieldId + 'Error');
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }

    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.add('error');
    }
}

function clearFieldError(fieldId) {
    const errorElement = document.getElementById(fieldId + 'Error');
    if (errorElement) {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
    }

    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.remove('error');
    }
}

function clearAllErrors() {
    const messageArea = document.getElementById('messageArea');
    messageArea.style.display = 'none';

    const errorElements = document.querySelectorAll('.field-error');
    errorElements.forEach(element => {
        element.textContent = '';
        element.style.display = 'none';
    });

    const errorFields = document.querySelectorAll('.error');
    errorFields.forEach(field => {
        field.classList.remove('error');
    });
}

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
