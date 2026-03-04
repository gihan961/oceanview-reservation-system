let currentAuth = null;

document.addEventListener('DOMContentLoaded', async () => {
    currentAuth = await initializePageAuth();
    if (!currentAuth) return;

    if (!hasPermission(currentAuth.role, 'canManageAccounts')) {
        alert('Access Denied: You do not have permission to manage accounts.');
        window.location.href = 'dashboard.html';
        return;
    }

    loadUsers();
});

async function loadUsers() {
    try {
        const res = await fetch('../api/register', { credentials: 'include' });
        if (!res.ok) throw new Error('Failed to fetch users');
        const users = await res.json();
        renderUsers(users);
    } catch (err) {
        console.error(err);
        document.getElementById('usersTableBody').innerHTML =
            '<tr><td colspan="3" style="text-align:center;padding:20px;color:#dc3545;">Failed to load users</td></tr>';
    }
}

function renderUsers(users) {
    const tbody = document.getElementById('usersTableBody');
    if (!users.length) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align:center;padding:20px;">No users found.</td></tr>';
        return;
    }
    tbody.innerHTML = users.map(u => `
        <tr>
            <td>${u.id}</td>
            <td>${u.username}</td>
            <td><span class="role-tag ${u.role.toLowerCase()}">${u.role}</span></td>
        </tr>
    `).join('');
}

async function handleCreateAccount(e) {
    e.preventDefault();

    const fullName = document.getElementById('fullName').value.trim();
    const username = document.getElementById('newUsername').value.trim();
    const password = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const role = document.getElementById('accountRole').value;

    if (!fullName || !username || !password || !confirmPassword || !role) {
        showAlert('Please fill in all fields.', 'error');
        return;
    }
    if (password.length < 6) {
        showAlert('Password must be at least 6 characters.', 'error');
        return;
    }
    if (password !== confirmPassword) {
        showAlert('Passwords do not match.', 'error');
        return;
    }

    const btn = document.getElementById('btnCreate');
    btn.disabled = true;
    btn.textContent = 'Creating...';

    try {
        const formData = new URLSearchParams();
        formData.append('fullName', fullName);
        formData.append('username', username);
        formData.append('password', password);
        formData.append('confirmPassword', confirmPassword);
        formData.append('role', role);

        const res = await fetch('../api/register', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
        const data = await res.json();

        if (res.ok && data.success) {
            showAlert(`Account created successfully! Username: ${data.username}, Role: ${data.role}`, 'success');
            document.getElementById('createAccountForm').reset();
            loadUsers();
        } else {
            showAlert(data.message || 'Failed to create account.', 'error');
        }
    } catch (err) {
        console.error(err);
        showAlert('Network error. Please try again.', 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Create Account';
    }
}

function showAlert(message, type) {
    const area = document.getElementById('alertArea');
    area.innerHTML = `<div class="alert alert-${type === 'error' ? 'error' : 'success'}">${message}</div>`;
    setTimeout(() => { area.innerHTML = ''; }, 6000);
}
