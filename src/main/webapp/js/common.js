// Common JavaScript utilities for SYOS POS System

// API Base URL
const API_BASE = '/api';

// Utility functions
const Utils = {
    /**
     * Show error message to user
     */
    showError: function(message, elementId = 'errorMessage') {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
            setTimeout(() => {
                errorElement.style.display = 'none';
            }, 5000);
        }
    },

    /**
     * Show success message to user
     */
    showSuccess: function(message, elementId = 'successMessage') {
        const successElement = document.getElementById(elementId);
        if (successElement) {
            successElement.textContent = message;
            successElement.style.display = 'block';
            setTimeout(() => {
                successElement.style.display = 'none';
            }, 3000);
        }
    },

    /**
     * Show loading state
     */
    showLoading: function(show = true, elementId = 'loadingMessage') {
        const loadingElement = document.getElementById(elementId);
        if (loadingElement) {
            loadingElement.style.display = show ? 'block' : 'none';
        }
    },

    /**
     * Format currency in LKR
     */
    formatCurrency: function(amount) {
        return 'LKR ' + parseFloat(amount).toFixed(2);
    },

    /**
     * Format date for display
     */
    formatDate: function(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    },

    /**
     * Validate form data
     */
    validateForm: function(formData, requiredFields) {
        const errors = [];
        
        requiredFields.forEach(field => {
            if (!formData[field] || formData[field].trim() === '') {
                errors.push(`${field} is required`);
            }
        });

        return errors;
    },

    /**
     * Get session data
     */
    getSession: function() {
        const sessionData = localStorage.getItem('syos_session');
        return sessionData ? JSON.parse(sessionData) : null;
    },

    /**
     * Set session data
     */
    setSession: function(sessionData) {
        localStorage.setItem('syos_session', JSON.stringify(sessionData));
    },

    /**
     * Clear session data
     */
    clearSession: function() {
        localStorage.removeItem('syos_session');
        sessionStorage.clear();
    },

    /**
     * Check if user is logged in
     */
    isLoggedIn: function() {
        const session = this.getSession();
        return session && session.userId && session.role;
    },

    /**
     * Get user role from session
     */
    getUserRole: function() {
        const session = this.getSession();
        return session ? session.role : null;
    },

    /**
     * Redirect to appropriate dashboard based on role
     */
    redirectToDashboard: function(role) {
        switch(role) {
            case 'CASHIER':
                window.location.href = 'cashier/dashboard.html';
                break;
            case 'MANAGER':
                window.location.href = 'manager/dashboard.html';
                break;
            case 'ADMIN':
                window.location.href = 'admin/dashboard.html';
                break;
            case 'ONLINE_CUSTOMER':
                window.location.href = 'customer/dashboard.html';
                break;
            default:
                window.location.href = 'index.html';
        }
    }
};

// AJAX Helper
const Ajax = {
    /**
     * Make HTTP request
     */
    request: async function(url, options = {}) {
        const defaultOptions = {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'same-origin'
        };

        const config = { ...defaultOptions, ...options };
        
        try {
            const response = await fetch(url, config);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            return { success: true, data: data };
        } catch (error) {
            console.error('AJAX Error:', error);
            return { success: false, error: error.message };
        }
    },

    /**
     * GET request
     */
    get: function(url) {
        return this.request(url);
    },

    /**
     * POST request
     */
    post: function(url, data) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    /**
     * PUT request
     */
    put: function(url, data) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    /**
     * DELETE request
     */
    delete: function(url) {
        return this.request(url, {
            method: 'DELETE'
        });
    }
};

// Session management
const SessionManager = {
    /**
     * Check session on page load
     */
    checkSession: function() {
        if (Utils.isLoggedIn()) {
            const role = Utils.getUserRole();
            // If on login page and already logged in, redirect to dashboard
            if (window.location.pathname.includes('index.html') || 
                window.location.pathname === '/' || 
                window.location.pathname.includes('login.html')) {
                Utils.redirectToDashboard(role);
            }
        } else {
            // If not on login/register page and not logged in, redirect to login
            if (!window.location.pathname.includes('index.html') && 
                !window.location.pathname.includes('login.html') &&
                !window.location.pathname.includes('register.html') &&
                window.location.pathname !== '/') {
                window.location.href = 'index.html';
            }
        }
    },

    /**
     * Logout user
     */
    logout: async function() {
        try {
            const result = await Ajax.post(API_BASE + '/auth/logout', {});
            Utils.clearSession();
            window.location.href = 'index.html';
        } catch (error) {
            console.error('Logout error:', error);
            // Force logout even if server request fails
            Utils.clearSession();
            window.location.href = 'index.html';
        }
    }
};

// Initialize session check on page load
document.addEventListener('DOMContentLoaded', function() {
    SessionManager.checkSession();
});

// Global error handler
window.addEventListener('error', function(e) {
    console.error('Global error:', e.error);
    Utils.showError('An unexpected error occurred. Please try again.');
});

// Export for use in other scripts
window.Utils = Utils;
window.Ajax = Ajax;
window.SessionManager = SessionManager;