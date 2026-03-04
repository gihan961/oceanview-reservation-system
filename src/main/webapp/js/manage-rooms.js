let currentAuth = null;
let allRooms = [];
let deleteRoomId = null;

document.addEventListener('DOMContentLoaded', async () => {
    currentAuth = await initializePageAuth();
    if (!currentAuth) return;

    if (!hasPermission(currentAuth.role, 'canManageRooms')) {
        alert('Access Denied: You do not have permission to manage rooms.');
        window.location.href = 'dashboard.html';
        return;
    }

    loadRooms();
});

async function loadRooms() {
    try {
        const res = await fetch('../api/rooms', { credentials: 'include' });
        if (!res.ok) throw new Error('Failed to fetch rooms');
        const data = await res.json();
        allRooms = data.rooms || data;
        renderTable(allRooms);
        updateStats(allRooms);
    } catch (err) {
        console.error(err);
        showAlert('Error loading rooms. Please try again.', 'error');
    }
}

function updateStats(rooms) {
    document.getElementById('statTotal').textContent = rooms.length;
    document.getElementById('statAvailable').textContent =
        rooms.filter(r => r.status === 'AVAILABLE').length;
    document.getElementById('statOccupied').textContent =
        rooms.filter(r => r.status === 'OCCUPIED').length;
    document.getElementById('statMaintenance').textContent =
        rooms.filter(r => r.status === 'MAINTENANCE').length;
}

function renderTable(rooms) {
    const tbody = document.getElementById('roomsTableBody');
    if (!rooms.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="no-data">No rooms found.</td></tr>';
        return;
    }
    tbody.innerHTML = rooms.map(r => `
        <tr>
            <td>${r.id}</td>
            <td>${r.roomType}</td>
            <td>LKR ${Number(r.pricePerNight).toLocaleString('en-LK', {minimumFractionDigits:2})}</td>
            <td><span class="badge badge-${r.status.toLowerCase()}">${r.status}</span></td>
            <td class="action-btns">
                <button class="btn-edit" onclick="openEditModal(${r.id})">✏️ Edit</button>
                <button class="btn-delete" onclick="openDeleteModal(${r.id})">🗑️ Delete</button>
            </td>
        </tr>
    `).join('');
}

function openAddModal() {
    document.getElementById('modalTitle').textContent = 'Add New Room';
    document.getElementById('btnSubmitRoom').textContent = 'Add Room';
    document.getElementById('editRoomId').value = '';
    document.getElementById('roomType').value = '';
    document.getElementById('pricePerNight').value = '';
    document.getElementById('roomStatus').value = 'AVAILABLE';
    document.getElementById('roomModal').style.display = 'block';
}

function openEditModal(id) {
    const room = allRooms.find(r => r.id === id);
    if (!room) return;
    document.getElementById('modalTitle').textContent = 'Edit Room #' + room.id;
    document.getElementById('btnSubmitRoom').textContent = 'Save Changes';
    document.getElementById('editRoomId').value = room.id;
    document.getElementById('roomType').value = room.roomType;
    document.getElementById('pricePerNight').value = room.pricePerNight;
    document.getElementById('roomStatus').value = room.status;
    document.getElementById('roomModal').style.display = 'block';
}

function closeModal() {
    document.getElementById('roomModal').style.display = 'none';
}

async function handleRoomSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('editRoomId').value;
    const roomType = document.getElementById('roomType').value;
    const pricePerNight = document.getElementById('pricePerNight').value;
    const roomStatus = document.getElementById('roomStatus').value;

    if (!roomType || !pricePerNight) {
        showAlert('Please fill in all required fields.', 'error');
        return;
    }

    const params = new URLSearchParams();
    params.append('roomType', roomType);
    params.append('pricePerNight', pricePerNight);
    params.append('status', roomStatus);

    try {
        let url, method;
        if (id) {

            url = `../api/rooms/${id}`;
            method = 'PUT';
        } else {

            url = '../api/rooms';
            method = 'POST';
        }

        const res = await fetch(url, {
            method,
            credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        });
        const data = await res.json();

        if (res.ok && data.success) {
            showAlert(data.message || (id ? 'Room updated!' : 'Room added!'), 'success');
            closeModal();
            loadRooms();
        } else {
            showAlert(data.message || 'Operation failed.', 'error');
        }
    } catch (err) {
        console.error(err);
        showAlert('Network error. Please try again.', 'error');
    }
}

function openDeleteModal(id) {
    deleteRoomId = id;
    const room = allRooms.find(r => r.id === id);
    document.getElementById('deleteRoomInfo').textContent =
        room ? `Room #${room.id} — ${room.roomType} (LKR ${Number(room.pricePerNight).toLocaleString()})` : '';
    document.getElementById('deleteModal').style.display = 'block';
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    deleteRoomId = null;
}

async function confirmDelete() {
    if (!deleteRoomId) return;
    try {
        const res = await fetch(`../api/rooms/${deleteRoomId}`, {
            method: 'DELETE',
            credentials: 'include'
        });
        const data = await res.json();

        if (res.ok && data.success) {
            showAlert('Room deleted successfully.', 'success');
            closeDeleteModal();
            loadRooms();
        } else {
            showAlert(data.message || 'Failed to delete room.', 'error');
        }
    } catch (err) {
        console.error(err);
        showAlert('Network error. Please try again.', 'error');
    }
}

function showAlert(message, type) {
    const area = document.getElementById('alertArea');
    area.innerHTML = `<div class="alert alert-${type === 'error' ? 'error' : 'success'}">${message}</div>`;
    setTimeout(() => { area.innerHTML = ''; }, 5000);
}

window.addEventListener('click', (e) => {
    if (e.target === document.getElementById('roomModal')) closeModal();
    if (e.target === document.getElementById('deleteModal')) closeDeleteModal();
});
