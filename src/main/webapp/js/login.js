// Login page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('errorMessage');
    const loadingMessage = document.getElementById('loadingMessage');
    
    // Handle form submission
    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        // Clear previous messages
        errorMessage.style.display = 'none';
        Utils.showLoading(true);
        
        // Get form data
        const formData = new FormData(loginForm);
        const credentials = {
            username: formData.get('username'),
            password: formData.get('password'),
            userType: formData.get('userType')
        };
        
        // Validate form
        const requiredFields = ['username', 'password', 'userType'];
        const errors = Utils.validateForm(credentials, requiredFields);
        
        if (errors.length > 0) {
            Utils.showLoading(false);
            Utils.showError(errors.join(', '));
            return;
        }
        
        try {
            // Send login request
            const result = await Ajax.post(API_BASE + '/auth/login', credentials);
            
            Utils.showLoading(false);
            
            if (result.success) {
                const sessionData = result.data;
                
                // Store session data
                Utils.setSession({
                    userId: sessionData.userId,
                    username: sessionData.username,
                    fullName: sessionData.fullName,
                    role: sessionData.role,
                    loginTime: new Date().toISOString()
                });
                
                // Redirect to appropriate dashboard
                Utils.redirectToDashboard(sessionData.role);
                
            } else {
                Utils.showError(result.error || 'Login failed. Please try again.');
            }
            
        } catch (error) {
            Utils.showLoading(false);
            Utils.showError('Network error. Please check your connection and try again.');
            console.error('Login error:', error);
        }
    });
    
    // Handle enter key in form fields
    const formInputs = loginForm.querySelectorAll('input, select');
    formInputs.forEach(input => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                loginForm.dispatchEvent(new Event('submit'));
            }
        });
    });
    
    // Auto-focus username field
    const usernameField = document.getElementById('username');
    if (usernameField) {
        usernameField.focus();
    }
    
    // Demo credentials helper (for development/testing)
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
        addDemoCredentialsHelper();
    }
});

/**
 * Add demo credentials helper for development
 */
function addDemoCredentialsHelper() {
    const demoCredentials = [
        { username: 'cashier', role: 'CASHIER', label: 'Demo Cashier' },
        { username: 'manager', role: 'MANAGER', label: 'Demo Manager' },
        { username: 'admin', role: 'ADMIN', label: 'Demo Admin' }
    ];
    
    const demoContainer = document.createElement('div');
    demoContainer.className = 'demo-credentials';
    demoContainer.innerHTML = '<p style="margin-top: 20px; font-size: 12px; color: #666;">Demo Credentials:</p>';
    
    demoCredentials.forEach(cred => {
        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = cred.label;
        button.style.cssText = 'margin: 2px; padding: 4px 8px; font-size: 10px; background: #f0f0f0; border: 1px solid #ccc; border-radius: 3px; cursor: pointer;';
        
        button.addEventListener('click', function() {
            document.getElementById('username').value = cred.username;
            document.getElementById('password').value = 'demo123'; // Default demo password
            document.getElementById('userType').value = cred.role;
        });
        
        demoContainer.appendChild(button);
    });
    
    document.querySelector('.login-card').appendChild(demoContainer);
}