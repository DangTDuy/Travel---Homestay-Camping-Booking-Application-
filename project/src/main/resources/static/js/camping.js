// URL cơ sở cho API
const BASE_URL = "/camping/api";

// Tải danh sách khu cắm trại khi trang được mở
window.onload = function() {
    loadCampings();
    
    // Set default dates for booking form
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    const dayAfterTomorrow = new Date(today);
    dayAfterTomorrow.setDate(today.getDate() + 2);
    
    document.getElementById("bookStartDate").valueAsDate = tomorrow;
    document.getElementById("bookEndDate").valueAsDate = dayAfterTomorrow;
};

// Tải danh sách khu cắm trại
function loadCampings() {
    fetch(BASE_URL, {
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                throw new Error("Vui lòng đăng nhập để xem danh sách khu cắm trại");
            }
            throw new Error('Lỗi mạng: ' + response.status);
        }
        return response.json();
    })
    .then(data => {
        const campingList = document.getElementById("campingList");
        const emptyState = document.getElementById("emptyState");
        
        campingList.innerHTML = "";
        
        if (data && data.length > 0) {
            emptyState.style.display = "none";
            
            data.forEach(camping => {
                if (camping) {
                    const div = document.createElement("div");
                    div.className = "bg-white rounded-lg shadow-lg overflow-hidden flex flex-col transform transition duration-300 hover:scale-105 hover:shadow-xl";
                    
                    const price = camping.price ? camping.price.toLocaleString() : '0';
                    const imageUrl = camping.imageUrls && camping.imageUrls.length > 0 
                        ? camping.imageUrls[0] 
                        : 'https://images.unsplash.com/photo-1504280390367-5d8f8c9d287f?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=60';
                    
                    // Tạo chuỗi đánh giá sao (giả sử camping có trường rating)
                    const rating = camping.rating || 0;
                    let starHtml = '';
                    for (let i = 1; i <= 5; i++) {
                        if (i <= rating) {
                            starHtml += '<i class="fas fa-star text-yellow-500 text-xs mr-0.5"></i>';
                        } else if (i <= rating + 0.5) {
                            starHtml += '<i class="fas fa-star-half-alt text-yellow-500 text-xs mr-0.5"></i>';
                        } else {
                            starHtml += '<i class="far fa-star text-yellow-500 text-xs mr-0.5"></i>';
                        }
                    }
                    
                    div.innerHTML = `
                        <div class="relative">
                            <img src="${imageUrl}" class="w-full h-48 object-cover" alt="${camping.name || 'Khu cắm trại'}">
                            <div class="absolute top-2 right-2 bg-primary bg-opacity-80 text-white text-xs font-semibold px-3 py-1 rounded-full shadow-md">
                                ${price} VNĐ/đêm
                            </div>
                            <button class="absolute top-2 left-2 bg-white bg-opacity-70 p-1.5 rounded-full text-gray-600 hover:text-red-500 hover:bg-opacity-90 transition duration-200">
                                <i class="far fa-heart text-lg"></i>
                            </button>
                        </div>
                        <div class="p-4 flex flex-col flex-grow">
                            <h3 class="text-lg font-semibold text-dark mb-1 truncate">${camping.name || 'Khu cắm trại'}</h3>
                            <p class="text-sm text-gray-500 mb-2 flex items-center flex-grow-0">
                                <i class="fas fa-map-marker-alt mr-1.5 text-gray-400 text-xs"></i>
                                <span>${camping.location || 'Chưa có địa điểm'}</span>
                            </p>
                            
                            <div class="flex items-center text-sm mb-3 mt-auto">
                                <div class="flex text-yellow-500">
                                    ${starHtml}
                                </div>
                                <span class="text-gray-600 font-medium ml-1.5">${rating.toFixed(1)}</span>
                                ${camping.bookingCount ? `<span class="text-gray-500 text-xs ml-2">· ${camping.bookingCount} lượt đặt</span>` : ''}
                            </div>
                            
                            <div class="flex space-x-2 mt-2">
                                <a href="/camping/${camping.id}" class="flex-1 text-center bg-white border border-primary text-primary hover:bg-gray-50 font-medium py-2 px-3 rounded-md transition duration-300 text-sm">
                                    <i class="fas fa-info-circle mr-1"></i> Chi tiết
                                </a>
                                <button onclick="showBookForm(${camping.id})" class="flex-1 text-center bg-primary hover:bg-blue-700 text-white font-medium py-2 px-3 rounded-md transition duration-300 text-sm">
                                    <i class="fas fa-calendar-check mr-1"></i> Đặt chỗ
                                </button>
                            </div>
                        </div>
                    `;
                    
                    campingList.appendChild(div);
                }
            });
        } else {
            emptyState.style.display = "block";
        }
    })
    .catch(error => {
        console.error("Lỗi:", error);
        const campingList = document.getElementById("campingList");
        const emptyState = document.getElementById("emptyState");
        
        emptyState.style.display = "block";
        emptyState.innerHTML = `
            <i class="fas fa-exclamation-circle text-red-500 text-6xl mb-4"></i>
            <h3 class="text-xl font-semibold text-dark mb-2">Không thể tải danh sách khu cắm trại</h3>
            <p class="text-gray-500">${error.message}</p>
        `;
        
        showMessage(error.message, "error");
    });
}

// Tìm kiếm khu cắm trại
function searchCampings() {
    const searchTerm = document.getElementById("searchTerm").value;
    const priceRange = document.getElementById("priceRange").value;
    const rating = document.getElementById("rating").value;
    
    let url = `${BASE_URL}?page=0&size=10`;
    
    if (searchTerm) {
        url += `&searchTerm=${encodeURIComponent(searchTerm)}`;
    }
    
    if (priceRange) {
        const [minPrice, maxPrice] = priceRange.split("-");
        if (maxPrice) {
            url += `&minPrice=${minPrice}&maxPrice=${maxPrice}`;
        } else {
            url += `&minPrice=${minPrice.replace('+', '')}`;
        }
    }
    
    if (rating) {
        url += `&minRating=${rating}`;
    }
    
    fetch(url, {
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Lỗi mạng: ' + response.status);
        }
        return response.json();
    })
    .then(data => {
        const campingList = document.getElementById("campingList");
        const emptyState = document.getElementById("emptyState");
        
        campingList.innerHTML = "";
        
        if (data && data.length > 0) {
            emptyState.style.display = "none";
            
            data.forEach(camping => {
                if (camping) {
                    const div = document.createElement("div");
                    div.className = "bg-white rounded-lg shadow-lg overflow-hidden flex flex-col transform transition duration-300 hover:scale-105 hover:shadow-xl";
                    
                    const price = camping.price ? camping.price.toLocaleString() : '0';
                    const imageUrl = camping.imageUrls && camping.imageUrls.length > 0 
                        ? camping.imageUrls[0] 
                        : 'https://images.unsplash.com/photo-1504280390367-5d8f8c9d287f?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=60';
                    
                    // Tạo chuỗi đánh giá sao
                    const rating = camping.rating || 0;
                    let starHtml = '';
                    for (let i = 1; i <= 5; i++) {
                        if (i <= rating) {
                            starHtml += '<i class="fas fa-star text-yellow-500 text-xs mr-0.5"></i>';
                        } else if (i <= rating + 0.5) {
                            starHtml += '<i class="fas fa-star-half-alt text-yellow-500 text-xs mr-0.5"></i>';
                        } else {
                            starHtml += '<i class="far fa-star text-yellow-500 text-xs mr-0.5"></i>';
                        }
                    }
                    
                    div.innerHTML = `
                        <div class="relative">
                            <img src="${imageUrl}" class="w-full h-48 object-cover" alt="${camping.name || 'Khu cắm trại'}">
                            <div class="absolute top-2 right-2 bg-primary bg-opacity-80 text-white text-xs font-semibold px-3 py-1 rounded-full shadow-md">
                                ${price} VNĐ/đêm
                            </div>
                            <button class="absolute top-2 left-2 bg-white bg-opacity-70 p-1.5 rounded-full text-gray-600 hover:text-red-500 hover:bg-opacity-90 transition duration-200">
                                <i class="far fa-heart text-lg"></i>
                            </button>
                        </div>
                        <div class="p-4 flex flex-col flex-grow">
                            <h3 class="text-lg font-semibold text-dark mb-1 truncate">${camping.name || 'Khu cắm trại'}</h3>
                            <p class="text-sm text-gray-500 mb-2 flex items-center flex-grow-0">
                                <i class="fas fa-map-marker-alt mr-1.5 text-gray-400 text-xs"></i>
                                <span>${camping.location || 'Chưa có địa điểm'}</span>
                            </p>
                            
                            <div class="flex items-center text-sm mb-3 mt-auto">
                                <div class="flex text-yellow-500">
                                    ${starHtml}
                                </div>
                                <span class="text-gray-600 font-medium ml-1.5">${rating.toFixed(1)}</span>
                                ${camping.bookingCount ? `<span class="text-gray-500 text-xs ml-2">· ${camping.bookingCount} lượt đặt</span>` : ''}
                            </div>
                            
                            <div class="flex space-x-2 mt-2">
                                <a href="/camping/${camping.id}" class="flex-1 text-center bg-white border border-primary text-primary hover:bg-gray-50 font-medium py-2 px-3 rounded-md transition duration-300 text-sm">
                                    <i class="fas fa-info-circle mr-1"></i> Chi tiết
                                </a>
                                <button onclick="showBookForm(${camping.id})" class="flex-1 text-center bg-primary hover:bg-blue-700 text-white font-medium py-2 px-3 rounded-md transition duration-300 text-sm">
                                    <i class="fas fa-calendar-check mr-1"></i> Đặt chỗ
                                </button>
                            </div>
                        </div>
                    `;
                    
                    campingList.appendChild(div);
                }
            });
        } else {
            emptyState.style.display = "block";
            emptyState.innerHTML = `
                <i class="fas fa-search text-6xl text-gray-300 mb-4"></i>
                <h3 class="text-xl font-semibold text-dark mb-2">Không tìm thấy kết quả</h3>
                <p class="text-gray-500">Không tìm thấy khu cắm trại phù hợp với tiêu chí tìm kiếm của bạn. Vui lòng thử lại với các tiêu chí khác.</p>
            `;
        }
    })
    .catch(error => {
        console.error("Lỗi:", error);
        showMessage("Lỗi khi tìm kiếm: " + error.message, "error");
    });
}

// Thêm khu cắm trại mới
function addCamping() {
    const name = document.getElementById("name").value;
    const location = document.getElementById("location").value;
    const price = document.getElementById("price").value;
    const capacity = document.getElementById("capacity").value;
    const availableSlots = document.getElementById("availableSlots").value;
    const description = document.getElementById("description").value;
    const facilities = document.getElementById("facilities").value;
    const equipment = document.getElementById("equipment").value;
    const rules = document.getElementById("rules").value;
    
    // Validate dữ liệu
    if (!name || !location || !price || !capacity || !availableSlots || !description) {
        showMessage("Vui lòng điền đầy đủ thông tin bắt buộc", "error");
        return;
    }
    
    const camping = {
        name,
        location,
        price,
        capacity,
        availableSlots,
        description,
        facilities,
        equipment,
        rules
    };
    
    // Gửi request POST
    fetch(BASE_URL, {
        method: "POST",
        headers: { 
            "Content-Type": "application/json",
            ...(document.querySelector('meta[name="_csrf"]') && {
                [document.querySelector('meta[name="_csrf_header"]').content]: document.querySelector('meta[name="_csrf"]').content
            })
        },
        body: JSON.stringify(camping),
        credentials: 'include'
    })
    .then(response => {
        // Kiểm tra nếu response là HTML (trang đăng nhập)
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("text/html") !== -1) {
            throw new Error("Bạn cần đăng nhập để thêm khu cắm trại");
        }
        
        if (!response.ok) {
            throw new Error('Không thể thêm khu cắm trại: ' + response.status);
        }
        
        return response.json();
    })
    .then(data => {
        showMessage("Đã thêm khu cắm trại thành công!", "success");
        
        // Reset form
        document.getElementById("name").value = "";
        document.getElementById("location").value = "";
        document.getElementById("price").value = "";
        document.getElementById("capacity").value = "";
        document.getElementById("availableSlots").value = "";
        document.getElementById("description").value = "";
        document.getElementById("facilities").value = "";
        document.getElementById("equipment").value = "";
        document.getElementById("rules").value = "";
        
        // Tải lại danh sách khu cắm trại
        loadCampings();
    })
    .catch(error => {
        console.error("Lỗi:", error);
        showMessage("Không thể thêm khu cắm trại: " + error.message, "error");
    });
}

// Hiển thị form đặt chỗ
function showBookForm(id) {
    document.getElementById("bookId").value = id;
    document.getElementById("bookForm").style.display = "block";
    // Scroll to form
    document.getElementById("bookForm").scrollIntoView({ behavior: 'smooth' });
}

// Đặt chỗ khu cắm trại
function bookCamping() {
    const campingId = document.getElementById("bookId").value;
    const startDate = document.getElementById("bookStartDate").value;
    const endDate = document.getElementById("bookEndDate").value;
    const numberOfPeople = document.getElementById("bookNumberOfPeople").value;
    const customerName = document.getElementById("bookCustomerName").value;
    
    // Validate dữ liệu
    if (!startDate || !endDate || !numberOfPeople || !customerName) {
        showMessage("Vui lòng điền đầy đủ thông tin đặt chỗ", "error");
        return;
    }
    
    // Kiểm tra ngày bắt đầu phải trước ngày kết thúc
    if (new Date(startDate) >= new Date(endDate)) {
        showMessage("Ngày bắt đầu phải trước ngày kết thúc", "error");
        return;
    }
    
    const booking = {
        campingId,
        startDate,
        endDate,
        numberOfPeople,
        customerName
    };
    
    // Gửi request POST
    fetch(`${BASE_URL}/${campingId}/book`, {
        method: "POST",
        headers: { 
            "Content-Type": "application/json",
            ...(document.querySelector('meta[name="_csrf"]') && {
                [document.querySelector('meta[name="_csrf_header"]').content]: document.querySelector('meta[name="_csrf"]').content
            })
        },
        body: JSON.stringify(booking),
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                throw new Error("Vui lòng đăng nhập để đặt chỗ");
            }
            return response.text().then(text => {
                throw new Error(text || 'Đã xảy ra lỗi khi đặt chỗ');
            });
        }
        return response.json();
    })
    .then(data => {
        showMessage("Đặt chỗ thành công! Mã đặt chỗ: " + data.id, "success");
        
        // Đóng form và reset
        document.getElementById("bookForm").style.display = "none";
        document.getElementById("bookNumberOfPeople").value = "";
        document.getElementById("bookCustomerName").value = "";
        
        // Tải lại danh sách khu cắm trại
        loadCampings();
    })
    .catch(error => {
        console.error("Lỗi:", error);
        showMessage("Không thể đặt chỗ: " + error.message, "error");
    });
}

// Hiển thị thông báo
function showMessage(message, type) {
    const toast = document.getElementById("toast");
    const messageText = document.getElementById("messageText");
    const toastIcon = document.getElementById("toastIcon");
    
    if (type === "success") {
        toast.className = "fixed bottom-4 right-4 bg-white shadow-lg rounded-lg p-4 max-w-md transform transition-all duration-300 scale-100 z-50";
        toastIcon.innerHTML = '<i class="fas fa-check-circle text-green-500 text-xl"></i>';
    } else if (type === "error") {
        toast.className = "fixed bottom-4 right-4 bg-white shadow-lg rounded-lg p-4 max-w-md transform transition-all duration-300 scale-100 z-50";
        toastIcon.innerHTML = '<i class="fas fa-exclamation-circle text-red-500 text-xl"></i>';
    } else {
        toast.className = "fixed bottom-4 right-4 bg-white shadow-lg rounded-lg p-4 max-w-md transform transition-all duration-300 scale-100 z-50";
        toastIcon.innerHTML = '<i class="fas fa-info-circle text-primary text-xl"></i>';
    }
    
    messageText.textContent = message;
    toast.style.display = "block";
    
    // Tự động ẩn sau 5 giây
    setTimeout(() => {
        toast.className = "fixed bottom-4 right-4 bg-white shadow-lg rounded-lg p-4 max-w-md transform transition-all duration-300 scale-0 z-50";
        setTimeout(() => {
            toast.style.display = "none";
        }, 300);
    }, 5000);
} 