// Login Page JavaScript
document.addEventListener('DOMContentLoaded', async function() {
    console.log('=== Login Page Loaded ===');
    
    // Check if already logged in (only if checkAuth is available)
    if (typeof checkAuth === 'function') {
        try {
            const auth = await checkAuth();
            if (auth && auth.authenticated) {
                console.log('User already authenticated, redirecting to dashboard');
                window.location.href = 'dashboard.html';
                return;
            }
        } catch (error) {
            console.log('Auth check skipped:', error.message);
        }
    }
    
    const loginForm = document.getElementById('loginForm');
    
    if (loginForm) {
        console.log('Login form found, attaching submit handler');
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            console.log('Login form submitted');
            
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;
            const errorDiv = document.getElementById('errorMessage');
            const successDiv = document.getElementById('successMessage');
            const submitBtn = document.getElementById('loginBtn');
            const btnText = document.getElementById('btnText');
            const btnLoader = document.getElementById('btnLoader');
            
            // Hide previous messages
            if (errorDiv) errorDiv.style.display = 'none';
            if (successDiv) successDiv.style.display = 'none';
            
            // Basic validation
            if (!username || !password) {
                showError(errorDiv, 'Please enter both username and password');
                return;
            }
            
            // Disable submit button and show loader
            if (submitBtn) submitBtn.disabled = true;
            if (btnText) btnText.textContent = 'Signing In...';
            if (btnLoader) btnLoader.style.display = 'inline-block';
            
            try {
                console.log('Attempting login for user:', username);
                
                const response = await fetch('../api/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
                });
                
                console.log('Response status:', response.status);
                const data = await response.json();
                console.log('Response data:', data);
                
                if (data.success) {
                    // Login successful
                    console.log('Login successful, redirecting to dashboard...');
                    showSuccess(successDiv, 'Login successful! Redirecting...');
                    setTimeout(() => {
                        window.location.href = 'dashboard.html';
                    }, 500);
                } else {
                    // Login failed
                    console.log('Login failed:', data.message);
                    showError(errorDiv, data.message || 'Invalid username or password');
                    if (submitBtn) submitBtn.disabled = false;
                    if (btnText) btnText.textContent = 'Sign In';
                    if (btnLoader) btnLoader.style.display = 'none';
                }
            } catch (error) {
                console.error('Login error:', error);
                showError(errorDiv, 'Login failed. Please try again.');
                if (submitBtn) submitBtn.disabled = false;
                if (btnText) btnText.textContent = 'Sign In';
                if (btnLoader) btnLoader.style.display = 'none';
            }
        });
    }
});

function showError(errorDiv, message) {
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'flex';
        errorDiv.className = 'alert alert-error';
    }
}

function showSuccess(successDiv, message) {
    if (successDiv) {
        successDiv.textContent = message;
        successDiv.style.display = 'flex';
        successDiv.className = 'alert alert-success';
    }
}
