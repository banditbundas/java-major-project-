// Authentication JavaScript

const API_BASE_URL = 'http://localhost:8080/api';

// Get token from localStorage
function getToken() {
    return localStorage.getItem('token');
}

// Set token in localStorage
function setToken(token) {
    localStorage.setItem('token', token);
}

// Remove token from localStorage
function removeToken() {
    localStorage.removeItem('token');
}

// Get authorization headers
function getAuthHeaders() {
    const token = getToken();
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
    };
}

// Handle login form
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const errorDiv = document.getElementById('errorMessage');
            errorDiv.style.display = 'none';

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetch(`${API_BASE_URL}/auth/login`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password })
                });

                const data = await response.json();

                if (response.ok) {
                    setToken(data.token);
                    window.location.href = '/dashboard';
                } else {
                    errorDiv.textContent = data.error || 'Login failed';
                    errorDiv.style.display = 'block';
                }
            } catch (error) {
                errorDiv.textContent = 'An error occurred. Please try again.';
                errorDiv.style.display = 'block';
            }
        });
    }

    // Handle register form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const errorDiv = document.getElementById('errorMessage');
            errorDiv.style.display = 'none';

            const formData = {
                username: document.getElementById('username').value,
                password: document.getElementById('password').value,
                email: document.getElementById('email').value,
                firstName: document.getElementById('firstName').value,
                lastName: document.getElementById('lastName').value,
                phoneNumber: document.getElementById('phoneNumber').value
            };

            try {
                const response = await fetch(`${API_BASE_URL}/auth/register`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(formData)
                });

                const data = await response.json();

                if (response.ok) {
                    alert('Registration successful! Please login.');
                    window.location.href = '/login';
                } else {
                    errorDiv.textContent = data.error || 'Registration failed';
                    errorDiv.style.display = 'block';
                }
            } catch (error) {
                errorDiv.textContent = 'An error occurred. Please try again.';
                errorDiv.style.display = 'block';
            }
        });
    }
});

// Logout function
function logout(event) {
    // Prevent default link behavior
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    
    try {
        // Clear all localStorage items related to auth
        localStorage.removeItem('token');
        localStorage.clear();
        
        // Force redirect to login page
        window.location.replace('/login');
    } catch (error) {
        console.error('Error during logout:', error);
        // Force clear and redirect even if there's an error
        localStorage.clear();
        window.location.replace('/login');
    }
    return false;
}

// Check if user is authenticated
function checkAuth() {
    const token = getToken();
    if (!token) {
        // Don't redirect if we're already on login/register page or home page
        const currentPath = window.location.pathname;
        if (currentPath !== '/login' && currentPath !== '/register' && currentPath !== '/') {
            // Use replace to prevent back button issues
            window.location.replace('/login');
        }
        return false;
    }
    return true;
}

// Verify token is still valid
async function verifyToken() {
    const token = getToken();
    if (!token) {
        return false;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/me`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.status === 401) {
            // Token is invalid, clear it
            removeToken();
            return false;
        }
        
        return response.ok;
    } catch (error) {
        console.error('Error verifying token:', error);
        return false;
    }
}

