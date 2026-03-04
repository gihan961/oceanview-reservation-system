document.addEventListener('DOMContentLoaded', function() {
    console.log('Register page loaded');

    const registerForm = document.getElementById('registerForm');
    const btnRegister = document.getElementById('btnRegister');
    const btnText = document.getElementById('btnText');
    const btnLoader = document.getElementById('btnLoader');
    const errorMessage = document.getElementById('errorMessage');
    const errorText = document.getElementById('errorText');
    const successMessage = document.getElementById('successMessage');
    const successText = document.getElementById('successText');

    const fullNameInput = document.getElementById('fullName');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const roleSelect = document.getElementById('role');

    if (!registerForm) {
        console.error('Register form not found!');
        return;
    }

    console.log('Register form found, attaching submit handler');

    registerForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        console.log('Registration form submitted');

        hideMessages();

        const fullName = fullNameInput.value.trim();
        const username = usernameInput.value.trim();
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        const role = roleSelect.value;

        console.log('Form values:', { fullName, username, role, passwordLength: password.length });

        if (!validateForm(fullName, username, password, confirmPassword, role)) {
            return;
        }

        setLoading(true);

        try {
            console.log('Attempting registration for user:', username);

            const formData = new URLSearchParams();
            formData.append('fullName', fullName);
            formData.append('username', username);
            formData.append('password', password);
            formData.append('confirmPassword', confirmPassword);
            formData.append('role', role);

            const response = await fetch('../api/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData.toString()
            });

            console.log('Response status:', response.status);
            const data = await response.json();
            console.log('Response data:', data);

            if (response.ok && data.success) {
                showSuccess(data.message || 'Registration successful! Redirecting to login...');
                console.log('Registration successful, redirecting to login...');

                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 2000);
            } else {
                showError(data.message || 'Registration failed. Please try again.');
            }
        } catch (error) {
            console.error('Registration error:', error);
            showError('An error occurred during registration. Please try again.');
        } finally {
            setLoading(false);
        }
    });

    function validateForm(fullName, username, password, confirmPassword, role) {

        if (fullName.length < 2) {
            showError('Full name must be at least 2 characters long');
            return false;
        }

        if (username.length < 3) {
            showError('Username must be at least 3 characters long');
            return false;
        }

        if (!/^[a-zA-Z0-9_]+$/.test(username)) {
            showError('Username can only contain letters, numbers, and underscores');
            return false;
        }

        if (password.length < 6) {
            showError('Password must be at least 6 characters long');
            return false;
        }

        if (password !== confirmPassword) {
            showError('Passwords do not match');
            return false;
        }

        if (!role || (role !== 'MANAGER' && role !== 'STAFF')) {
            showError('Please select a valid role');
            return false;
        }

        return true;
    }

    function showError(message) {
        console.error('Showing error:', message);
        if (errorText) errorText.textContent = message;
        if (errorMessage) errorMessage.style.display = 'flex';
        if (successMessage) successMessage.style.display = 'none';
    }

    function showSuccess(message) {
        console.log('Showing success:', message);
        if (successText) successText.textContent = message;
        if (successMessage) successMessage.style.display = 'flex';
        if (errorMessage) errorMessage.style.display = 'none';
    }

    function hideMessages() {
        if (errorMessage) errorMessage.style.display = 'none';
        if (successMessage) successMessage.style.display = 'none';
    }

    function setLoading(isLoading) {
        if (isLoading) {
            if (btnRegister) btnRegister.disabled = true;
            if (btnText) btnText.textContent = 'Creating Account...';
            if (btnLoader) btnLoader.style.display = 'inline-block';
        } else {
            if (btnRegister) btnRegister.disabled = false;
            if (btnText) btnText.textContent = 'Create Account';
            if (btnLoader) btnLoader.style.display = 'none';
        }
    }

    confirmPasswordInput.addEventListener('input', function() {
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        if (confirmPassword && password !== confirmPassword) {
            confirmPasswordInput.setCustomValidity('Passwords do not match');
        } else {
            confirmPasswordInput.setCustomValidity('');
        }
    });

    usernameInput.addEventListener('input', function() {
        const username = usernameInput.value;

        if (username && !/^[a-zA-Z0-9_]+$/.test(username)) {
            usernameInput.setCustomValidity('Only letters, numbers, and underscores allowed');
        } else {
            usernameInput.setCustomValidity('');
        }
    });
});
