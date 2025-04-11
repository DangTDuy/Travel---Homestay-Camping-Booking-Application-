/**
 * Booking Calendar JavaScript
 * Xử lý hiển thị lịch đã được đặt và kiểm tra trùng lịch
 */

document.addEventListener('DOMContentLoaded', function() {
    // Kiểm tra xem có phải trang đặt phòng không
    const homestayIdInput = document.getElementById('homestayId');
    if (!homestayIdInput) return;
    
    const homestayId = homestayIdInput.value;
    const checkInDateInput = document.getElementById('checkInDate');
    const checkOutDateInput = document.getElementById('checkOutDate');
    const checkInTimeInput = document.getElementById('checkInTime');
    const checkOutTimeInput = document.getElementById('checkOutTime');
    
    if (!checkInDateInput || !checkOutDateInput) return;
    
    // Lấy ngày hiện tại
    const today = new Date();
    const todayStr = today.toISOString().split('T')[0];
    
    // Thiết lập ngày tối thiểu cho input date
    checkInDateInput.min = todayStr;
    checkOutDateInput.min = todayStr;
    
    // Lấy danh sách booking đã có
    fetchBookings(homestayId);
    
    // Xử lý sự kiện thay đổi ngày check-in
    checkInDateInput.addEventListener('change', function() {
        // Đảm bảo ngày check-out luôn sau ngày check-in
        if (checkOutDateInput.value && new Date(checkOutDateInput.value) <= new Date(checkInDateInput.value)) {
            // Tự động thiết lập ngày check-out là ngày sau ngày check-in
            const nextDay = new Date(checkInDateInput.value);
            nextDay.setDate(nextDay.getDate() + 1);
            checkOutDateInput.value = nextDay.toISOString().split('T')[0];
        }
        
        // Thiết lập ngày tối thiểu cho check-out
        const minCheckOut = new Date(checkInDateInput.value);
        minCheckOut.setDate(minCheckOut.getDate() + 1);
        checkOutDateInput.min = minCheckOut.toISOString().split('T')[0];
        
        // Kiểm tra trùng lịch
        checkAvailability();
    });
    
    // Xử lý sự kiện thay đổi ngày check-out
    checkOutDateInput.addEventListener('change', function() {
        // Kiểm tra trùng lịch
        checkAvailability();
    });
    
    // Nếu có input thời gian, xử lý sự kiện thay đổi
    if (checkInTimeInput && checkOutTimeInput) {
        checkInTimeInput.addEventListener('change', checkAvailability);
        checkOutTimeInput.addEventListener('change', checkAvailability);
    }
});

/**
 * Lấy danh sách booking đã có
 */
function fetchBookings(homestayId) {
    fetch(`/api/homestays/${homestayId}/bookings`)
        .then(response => response.json())
        .then(bookings => {
            // Lưu danh sách booking vào biến toàn cục
            window.existingBookings = bookings;
            
            // Hiển thị thông tin booking đã có
            displayBookingInfo(bookings);
        })
        .catch(error => {
            console.error('Lỗi khi lấy danh sách booking:', error);
        });
}

/**
 * Hiển thị thông tin booking đã có
 */
function displayBookingInfo(bookings) {
    const bookingInfoElement = document.getElementById('booking-info');
    if (!bookingInfoElement) return;
    
    if (bookings.length === 0) {
        bookingInfoElement.innerHTML = '<p class="text-green-600">Chưa có lịch đặt phòng nào.</p>';
        return;
    }
    
    let html = '<div class="mt-4 mb-4">';
    html += '<h4 class="text-lg font-medium text-gray-800 mb-2">Lịch đã được đặt:</h4>';
    html += '<ul class="space-y-2">';
    
    bookings.forEach(booking => {
        const checkIn = new Date(booking.checkIn);
        const checkOut = new Date(booking.checkOut);
        
        html += `<li class="text-sm text-gray-600">
            <span class="inline-block px-2 py-1 bg-red-100 text-red-800 rounded-full mr-2">Đã đặt</span>
            ${formatDate(checkIn)} - ${formatDate(checkOut)}
        </li>`;
    });
    
    html += '</ul></div>';
    bookingInfoElement.innerHTML = html;
}

/**
 * Kiểm tra trùng lịch
 */
function checkAvailability() {
    const homestayId = document.getElementById('homestayId').value;
    const checkInDate = document.getElementById('checkInDate').value;
    const checkOutDate = document.getElementById('checkOutDate').value;
    
    if (!checkInDate || !checkOutDate) return;
    
    // Tạo đối tượng datetime đầy đủ
    let checkIn = new Date(checkInDate);
    let checkOut = new Date(checkOutDate);
    
    // Nếu có input thời gian, thêm thời gian vào
    const checkInTimeInput = document.getElementById('checkInTime');
    const checkOutTimeInput = document.getElementById('checkOutTime');
    
    if (checkInTimeInput && checkOutTimeInput) {
        const checkInTime = checkInTimeInput.value || '14:00';
        const checkOutTime = checkOutTimeInput.value || '12:00';
        
        checkIn = new Date(`${checkInDate}T${checkInTime}`);
        checkOut = new Date(`${checkOutDate}T${checkOutTime}`);
    }
    
    // Kiểm tra trùng lịch với danh sách booking đã có
    if (window.existingBookings) {
        const overlappingBookings = window.existingBookings.filter(booking => {
            const bookingCheckIn = new Date(booking.checkIn);
            const bookingCheckOut = new Date(booking.checkOut);
            
            // Kiểm tra xem có trùng lịch không
            return (checkIn < bookingCheckOut && checkOut > bookingCheckIn);
        });
        
        const availabilityMessage = document.getElementById('availability-message');
        if (!availabilityMessage) return;
        
        if (overlappingBookings.length > 0) {
            availabilityMessage.innerHTML = '<p class="text-red-600 font-medium">Lịch đã được đặt trong khoảng thời gian này. Vui lòng chọn ngày khác.</p>';
            document.getElementById('submitButton').disabled = true;
        } else {
            availabilityMessage.innerHTML = '<p class="text-green-600 font-medium">Lịch khả dụng!</p>';
            document.getElementById('submitButton').disabled = false;
        }
    }
    
    // Gọi API để kiểm tra trùng lịch
    fetch(`/api/bookings/check-availability?homestayId=${homestayId}&checkIn=${checkIn.toISOString()}&checkOut=${checkOut.toISOString()}`)
        .then(response => response.json())
        .then(data => {
            const availabilityMessage = document.getElementById('availability-message');
            if (!availabilityMessage) return;
            
            if (data.available) {
                availabilityMessage.innerHTML = '<p class="text-green-600 font-medium">Lịch khả dụng!</p>';
                document.getElementById('submitButton').disabled = false;
            } else {
                availabilityMessage.innerHTML = '<p class="text-red-600 font-medium">Lịch đã được đặt trong khoảng thời gian này. Vui lòng chọn ngày khác.</p>';
                document.getElementById('submitButton').disabled = true;
            }
        })
        .catch(error => {
            console.error('Lỗi khi kiểm tra trùng lịch:', error);
        });
}

/**
 * Format date để hiển thị
 */
function formatDate(date) {
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}
