/**
 * Toast Notification System
 * Sử dụng thư viện Toastify để hiển thị thông báo đẹp mắt
 */

// Toast notification function
function showToast(message, type = 'success') {
    const bgColor = type === 'success' ? 'linear-gradient(to right, #00b09b, #96c93d)' :
                   type === 'error' ? 'linear-gradient(to right, #ff5f6d, #ffc371)' :
                   type === 'info' ? 'linear-gradient(to right, #2193b0, #6dd5ed)' :
                   'linear-gradient(to right, #f7b733, #fc4a1a)';
    
    Toastify({
        text: message,
        duration: 3000,
        close: true,
        gravity: "top",
        position: "right",
        backgroundColor: bgColor,
        stopOnFocus: true
    }).showToast();
}

// Shorthand functions for different toast types
function showSuccessToast(message) {
    showToast(message, 'success');
}

function showErrorToast(message) {
    showToast(message, 'error');
}

function showInfoToast(message) {
    showToast(message, 'info');
}

function showWarningToast(message) {
    showToast(message, 'warning');
}

// Automatically convert all alerts to toasts if enabled
function enableToastForAlerts() {
    // Store the original alert function
    const originalAlert = window.alert;
    
    // Override the alert function
    window.alert = function(message) {
        showToast(message, 'info');
        // Optionally, still call the original alert
        // originalAlert(message);
    };
}

// Call this function to enable toast for all alerts
// enableToastForAlerts();
