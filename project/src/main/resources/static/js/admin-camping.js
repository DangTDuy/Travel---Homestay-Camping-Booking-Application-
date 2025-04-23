document.addEventListener('DOMContentLoaded', () => {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    let currentCampingId = null;
    const maxImages = 5; // Số lượng hình ảnh tối đa

    fetchCampings();

    // Sử lý form tìm kiếm
    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', (event) => {
            event.preventDefault(); // Ngăn form submit mặc định
            const location = document.getElementById('searchLocation').value;
            const priceRange = document.getElementById('priceRange').value;
            fetchCampingsWithFilters(location, priceRange); // Tìm kiếm với điều kiện lọc
        });
    }

    // Hàm reset tìm kiếm
    window.resetSearch = () => {
        document.getElementById('searchLocation').value = '';
        document.getElementById('priceRange').value = '';
        fetchCampings(); // Tải lại tất cả dữ liệu
    };

    // Hàm xử lý form tìm kiếm để sử dụng trong HTML
    window.handleSearchForm = (event) => {
        event.preventDefault();
        const location = document.getElementById('searchLocation').value;
        const priceRange = document.getElementById('priceRange').value;
        fetchCampingsWithFilters(location, priceRange);
    };

    // Hàm tìm kiếm với bộ lọc
    async function fetchCampingsWithFilters(location = '', priceRange = '') {
        try {
            showLoading();
            let url = '/camping/api';
            const params = new URLSearchParams();
            
            if (location) params.append('searchTerm', location);
            
            if (priceRange) {
                const [minPrice, maxPrice] = priceRange.split("-");
                if (maxPrice) {
                    params.append('minPrice', minPrice);
                    params.append('maxPrice', maxPrice);
                } else {
                    // Trường hợp "trên 2 triệu"
                    params.append('minPrice', minPrice.replace('+', ''));
                }
            }
            
            console.log('Tìm kiếm với các tham số:', {
                location: location,
                priceRange: priceRange
            });
            
            if (params.toString()) {
                url += '?' + params.toString();
            }
            
            const response = await fetch(url);
            if (!response.ok) throw new Error('Không thể tải danh sách khu cắm trại');
            const data = await response.json();
            
            console.log(`Tìm thấy ${data.length} khu cắm trại phù hợp với tìm kiếm`);
            if (priceRange === '2000000+') {
                // Lọc thêm một lần nữa ở client nếu là "trên 2 triệu"
                const filtered = data.filter(camping => camping.price >= 2000000);
                console.log(`Sau khi lọc thêm trên 2 triệu: ${filtered.length} khu cắm trại`);
                displayCampings(filtered);
            } else {
                displayCampings(data);
            }
        } catch (error) {
            console.error('Lỗi khi tìm kiếm khu cắm trại:', error);
            showToast('Lỗi', 'Không thể tìm kiếm khu cắm trại: ' + error.message, 'error');
        } finally {
            hideLoading();
        }
    }

    // Hàm tải dữ liệu khu cắm trại
    async function fetchCampings() {
        try {
            showLoading();
            const response = await fetch('/camping/api');
            if (!response.ok) throw new Error('Không thể tải danh sách khu cắm trại');
            const data = await response.json();
            displayCampings(data);
        } catch (error) {
            console.error('Lỗi khi tải khu cắm trại:', error);
            showToast('Lỗi', 'Không thể tải danh sách khu cắm trại: ' + error.message, 'error');
        } finally {
            hideLoading();
        }
    }

    // Hiển thị danh sách khu cắm trại
    function displayCampings(campings) {
        console.log('Dữ liệu danh sách khu cắm trại:', campings);
        const tableBody = document.getElementById('campingTableBody');
        tableBody.innerHTML = ''; // Xóa dữ liệu cũ

        if (!campings || campings.length === 0) {
            document.getElementById('noResults').classList.remove('hidden');
            document.getElementById('campingTableContainer').classList.add('hidden');
            return;
        }

        document.getElementById('noResults').classList.add('hidden');
        document.getElementById('campingTableContainer').classList.remove('hidden');

        campings.forEach(camping => {
            console.log('Camping ID:', camping.id, 'Image URLs:', camping.imageUrls);
            const row = document.createElement('tr');
            row.className = 'hover:bg-gray-50 transition-colors duration-150 ease-in-out';

            // Ô tên với hình ảnh
            const nameCell = document.createElement('td');
            nameCell.className = 'px-6 py-4';
            
            // Tạo flex container cho hình ảnh và tên
            const flexContainer = document.createElement('div');
            flexContainer.className = 'flex items-center space-x-3';
            
            // Container hình ảnh
            const imgContainer = document.createElement('div');
            imgContainer.className = 'flex-shrink-0 w-16 h-16';
            
            // Phần tử hình ảnh
            const img = document.createElement('img');
            img.className = 'w-16 h-16 object-cover rounded-lg';
            img.alt = camping.name;
            
            // Đặt nguồn hình ảnh với fallback
            if (camping.imageUrls && camping.imageUrls.length > 0) {
                img.src = camping.imageUrls[0];
            } else {
                img.src = 'https://images.unsplash.com/photo-1504280390367-5d8f8c9d287f?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=60';
            }
            
            imgContainer.appendChild(img);
            
            // Tên
            const nameText = document.createElement('div');
            nameText.className = 'font-medium text-gray-900';
            nameText.textContent = camping.name || 'N/A';
            
            flexContainer.appendChild(imgContainer);
            flexContainer.appendChild(nameText);
            nameCell.appendChild(flexContainer);

            // Ô địa điểm
            const locationCell = document.createElement('td');
            locationCell.className = 'px-6 py-4';
            locationCell.textContent = camping.location || 'N/A';

            // Ô giá
            const priceCell = document.createElement('td');
            priceCell.className = 'px-6 py-4';
            const formattedPrice = camping.price ? 
                new Intl.NumberFormat('vi-VN').format(camping.price) + ' VNĐ' : 'N/A';
            priceCell.textContent = formattedPrice;

            // Ô sức chứa
            const capacityCell = document.createElement('td');
            capacityCell.className = 'px-6 py-4';
            capacityCell.textContent = camping.maxPlaces ? `${camping.maxPlaces} người` : (camping.capacity ? `${camping.capacity} người` : 'N/A');

            // Ô đánh giá
            const ratingCell = document.createElement('td');
            ratingCell.className = 'px-6 py-4';
            const rating = camping.rating || 0;
            ratingCell.innerHTML = `
                <div class="flex items-center">
                    <span class="text-yellow-400 mr-1">
                        <i class="fas fa-star"></i>
                    </span>
                    <span>${rating.toFixed(1)}</span>
                </div>
            `;

            // Ô thao tác
            const actionsCell = document.createElement('td');
            actionsCell.className = 'px-6 py-4';
            actionsCell.innerHTML = `
                <div class="flex space-x-3">
                    <a href="/camping/${camping.id}" class="text-blue-600 hover:text-blue-900" title="Xem chi tiết" target="_blank">
                        <i class="fas fa-eye"></i>
                    </a>
                    <button onclick="openEditModal(${camping.id})" class="text-indigo-600 hover:text-indigo-900" title="Chỉnh sửa">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="showDeleteModal(${camping.id}, '${escapeJsString(camping.name)}')" class="text-red-600 hover:text-red-900" title="Xóa">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </div>
            `;

            row.appendChild(nameCell);
            row.appendChild(locationCell);
            row.appendChild(priceCell);
            row.appendChild(capacityCell);
            row.appendChild(ratingCell);
            row.appendChild(actionsCell);

            tableBody.appendChild(row);
        });
    }

    // Hàm escape chuỗi JavaScript sử dụng trong thuộc tính HTML
    function escapeJsString(str) {
        if (!str) return '';
        return str.replace(/\\/g, '\\\\') // Escape backslashes
                  .replace(/'/g, "\\'")   // Escape single quotes
                  .replace(/\n/g, '\\n') // Escape newlines
                  .replace(/\r/g, '\\r'); // Escape carriage returns
    }
    
    // --- Xử lý Modal --- 
    window.openAddModal = () => {
        currentCampingId = null;
        document.getElementById('modalTitle').innerHTML = '<i class="fas fa-campground mr-2"></i>Thêm Khu Cắm Trại mới';
        document.getElementById('campingForm').reset(); // Xóa trường form
        document.getElementById('campingId').value = ''; // Xóa ID ẩn
        document.getElementById('campingModal').classList.remove('hidden');
    };

    window.openEditModal = async (id) => {
        currentCampingId = id;
        document.getElementById('modalTitle').innerHTML = '<i class="fas fa-campground mr-2"></i>Chỉnh sửa Khu Cắm Trại';
        document.getElementById('campingForm').reset(); // Xóa dữ liệu trước đó
        document.getElementById('campingId').value = id;

        try {
            // Tải thông tin chi tiết khu cắm trại
            const response = await fetch(`/camping/api/${id}`);
            if (!response.ok) throw new Error('Không thể tải thông tin khu cắm trại');
            const camping = await response.json();

            // Điền thông tin vào form
            document.getElementById('name').value = camping.name || '';
            document.getElementById('location').value = camping.location || '';
            document.getElementById('description').value = camping.description || '';
            document.getElementById('price').value = camping.price || '';
            document.getElementById('capacity').value = camping.maxPlaces || camping.capacity || '';
            document.getElementById('availableSlots').value = camping.availableSlots || '';
            document.getElementById('facilities').value = camping.facilities ? (Array.isArray(camping.facilities) ? camping.facilities.join(', ') : camping.facilities) : '';
            document.getElementById('equipment').value = camping.equipment || '';
            document.getElementById('rules').value = camping.rules || '';
            
            // Xử lý URLs hình ảnh
            if (camping.imageUrls && camping.imageUrls.length > 0) {
                document.getElementById('imageUrls').value = camping.imageUrls.join('\n');
            }

            document.getElementById('campingModal').classList.remove('hidden');
        } catch (error) {
            console.error('Lỗi khi tải thông tin khu cắm trại:', error);
            showToast('Lỗi', 'Không thể tải thông tin khu cắm trại: ' + error.message, 'error');
        }
    };

    window.closeModal = () => {
        document.getElementById('campingModal').classList.add('hidden');
    };

    window.showDeleteModal = (id, name) => {
        currentCampingId = id;
        document.getElementById('deleteCampingName').textContent = name;
        document.getElementById('deleteModal').classList.remove('hidden');
    };

    window.closeDeleteModal = () => {
        document.getElementById('deleteModal').classList.add('hidden');
    };

    window.confirmDelete = async () => {
        if (!currentCampingId) return;

        try {
            const response = await fetch(`/camping/api/${currentCampingId}`, {
                method: 'DELETE',
                headers: {
                    [csrfHeader]: csrfToken
                }
            });

            if (!response.ok) throw new Error('Không thể xóa khu cắm trại');

            closeDeleteModal();
            showToast('Thành công', 'Khu cắm trại đã được xóa thành công', 'success');
            fetchCampings(); // Tải lại danh sách
        } catch (error) {
            console.error('Lỗi khi xóa khu cắm trại:', error);
            showToast('Lỗi', 'Không thể xóa khu cắm trại: ' + error.message, 'error');
        }
    };

    // Xử lý form submit
    window.handleFormSubmit = async (event) => {
        event.preventDefault();
        const form = event.target;
        const formData = new FormData(form);
        const camping = formDataToObject(formData);
        const campingId = document.getElementById('campingId').value;

        try {
            let url = '/camping/api';
            let method = 'POST';

            if (campingId) {
                url = `/camping/api/${campingId}`;
                method = 'PUT';
                camping.id = campingId;
            }

            // Xử lý danh sách hình ảnh
            if (document.getElementById('imageUrls').value) {
                camping.imageUrls = document.getElementById('imageUrls').value
                    .split('\n')
                    .filter(url => url.trim() !== '')
                    .slice(0, maxImages); // Giới hạn tối đa 5 hình ảnh
            } else {
                camping.imageUrls = [];
            }

            // Xử lý tiện ích nếu là chuỗi
            if (typeof camping.facilities === 'string' && camping.facilities.trim() !== '') {
                camping.facilities = camping.facilities.split(',').map(item => item.trim());
            }

            console.log('Gửi dữ liệu:', camping);

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(camping)
            });

            if (!response.ok) throw new Error('Không thể lưu khu cắm trại');

            closeModal();
            showToast('Thành công', 'Khu cắm trại đã được lưu thành công', 'success');
            fetchCampings(); // Tải lại danh sách
        } catch (error) {
            console.error('Lỗi khi lưu khu cắm trại:', error);
            showToast('Lỗi', 'Không thể lưu khu cắm trại: ' + error.message, 'error');
        }
    };

    // Chuyển FormData thành Object
    function formDataToObject(formData) {
        const obj = {};
        for (let [key, value] of formData.entries()) {
            // Chuyển đổi số nếu cần
            if (key === 'price' || key === 'capacity' || key === 'availableSlots') {
                obj[key] = value !== '' ? Number(value) : null;
            } else {
                obj[key] = value;
            }
        }
        return obj;
    }

    // Hiển thị toast thông báo
    window.showToast = function(title, message, type = 'info') {
        const toast = document.getElementById('toastNotification');
        const toastTitle = document.getElementById('toastTitle');
        const toastMessage = document.getElementById('toastMessage');
        const toastIcon = document.getElementById('toastIcon');
        
        // Thiết lập nội dung
        toastTitle.textContent = title;
        toastMessage.textContent = message;
        
        // Thiết lập icon dựa vào loại thông báo
        if (type === 'success') {
            toastIcon.innerHTML = '<div class="flex items-center justify-center h-10 w-10 rounded-full bg-green-100"><i class="fas fa-check text-green-500"></i></div>';
        } else if (type === 'error') {
            toastIcon.innerHTML = '<div class="flex items-center justify-center h-10 w-10 rounded-full bg-red-100"><i class="fas fa-exclamation-circle text-red-500"></i></div>';
        } else {
            toastIcon.innerHTML = '<div class="flex items-center justify-center h-10 w-10 rounded-full bg-blue-100"><i class="fas fa-info-circle text-blue-500"></i></div>';
        }
        
        // Hiển thị toast
        toast.classList.remove('hidden');
        
        // Tự động ẩn sau 5 giây
        setTimeout(() => {
            closeToast();
        }, 5000);
    };

    // Đóng toast notification
    window.closeToast = function() {
        const toast = document.getElementById('toastNotification');
        toast.classList.add('hidden');
    };

    // Hiển thị loading
    function showLoading() {
        document.getElementById('loadingTable').classList.remove('hidden');
        document.getElementById('campingTableContainer').classList.add('hidden');
        document.getElementById('noResults').classList.add('hidden');
    }

    // Ẩn loading
    function hideLoading() {
        document.getElementById('loadingTable').classList.add('hidden');
    }
}); 