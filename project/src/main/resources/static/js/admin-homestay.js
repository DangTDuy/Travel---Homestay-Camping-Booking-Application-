document.addEventListener('DOMContentLoaded', () => {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    let currentHomestayId = null;
    const maxImages = 5; // Maximum number of images allowed

    fetchHomestays();

    // Sửa lại selector cho form tìm kiếm
    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', (event) => {
            event.preventDefault(); // Prevent default form submission
            const location = document.getElementById('searchLocation').value;
            const priceRange = document.getElementById('priceRange').value;
            fetchHomestaysWithFilters(location, priceRange); // Fetch with search terms
        });
    }

    // Thêm hàm xử lý reset tìm kiếm
    window.resetSearch = () => {
        document.getElementById('searchLocation').value = '';
        document.getElementById('priceRange').value = '';
        fetchHomestays(); // Fetch all without filters
    };

    // Thêm hàm handleSearchForm để sử dụng trong HTML
    window.handleSearchForm = (event) => {
        event.preventDefault();
        const location = document.getElementById('searchLocation').value;
        const priceRange = document.getElementById('priceRange').value;
        fetchHomestaysWithFilters(location, priceRange);
    };

    // Thêm hàm tìm kiếm với bộ lọc
    async function fetchHomestaysWithFilters(location = '', priceRange = '') {
        try {
            showLoading();
            let url = '/admin/homestay/api/list';
            const params = new URLSearchParams();

            if (location) params.append('location', location);
            if (priceRange) params.append('priceRange', priceRange);

            console.log('Tìm kiếm với các tham số:', {
                location: location,
                priceRange: priceRange
            });

            if (params.toString()) {
                url += '?' + params.toString();
            }

            const response = await fetch(url);
            if (!response.ok) throw new Error('Không thể tải danh sách homestay');
            const data = await response.json();
            if (data.success) {
                console.log(`Tìm thấy ${data.data.length} homestay phù hợp với tìm kiếm`);
                if (priceRange === '2000000-') {
                    // Lọc thêm một lần nữa ở client nếu là "trên 2 triệu"
                    const filtered = data.data.filter(homestay => homestay.price >= 2000000);
                    console.log(`Sau khi lọc thêm trên 2 triệu: ${filtered.length} homestay`);
                    displayHomestays(filtered);
                } else {
                    displayHomestays(data.data);
                }
            } else {
                showErrorAlert(data.message || 'Không thể tải danh sách homestay');
            }
        } catch (error) {
            console.error('Lỗi khi tìm kiếm homestay:', error);
            showErrorAlert('Lỗi khi tìm kiếm homestay: ' + error.message);
        } finally {
            hideLoading();
        }
    }

    async function fetchHomestays(location = '') {
        try {
            showLoading();
            const url = location ? `/admin/homestay/api/list?location=${encodeURIComponent(location)}` : '/admin/homestay/api/list';
            const response = await fetch(url);
            if (!response.ok) throw new Error('Failed to fetch homestays');
            const data = await response.json();
            if (data.success) {
                displayHomestays(data.data);
            } else {
                showErrorAlert(data.message || 'Failed to fetch homestays');
            }
        } catch (error) {
            console.error('Error fetching homestays:', error);
            showErrorAlert('Error fetching homestays: ' + error.message);
        } finally {
            hideLoading();
        }
    }

    function displayHomestays(homestays) {
        console.log('Dữ liệu danh sách homestay:', homestays);
        const tableBody = document.getElementById('homestayTableBody');
        tableBody.innerHTML = ''; // Clear existing rows

        if (!homestays || homestays.length === 0) {
            document.getElementById('noResults').classList.remove('hidden');
            document.getElementById('homestayTableContainer').classList.add('hidden');
            return;
        }

        document.getElementById('noResults').classList.add('hidden');
        document.getElementById('homestayTableContainer').classList.remove('hidden');

        homestays.forEach(homestay => {
            console.log('Homestay ID:', homestay.id, 'Image URLs:', homestay.imageUrls);
            const row = document.createElement('tr');
            row.className = 'hover:bg-gray-50 transition-colors duration-150 ease-in-out';

            // Name cell with image
            const nameCell = document.createElement('td');
            nameCell.className = 'px-6 py-4';

            // Create flex container for image and name
            const flexContainer = document.createElement('div');
            flexContainer.className = 'flex items-center space-x-3';

            // Image container
            const imgContainer = document.createElement('div');
            imgContainer.className = 'flex-shrink-0 w-16 h-16';

            // Image element
            const img = document.createElement('img');
            img.className = 'w-16 h-16 object-cover rounded-lg';
            img.alt = homestay.name;

            // Set image source with fallback
            if (homestay.imageUrls && homestay.imageUrls.length > 0) {
                img.src = homestay.imageUrls[0];
            } else {
                img.src = '/images/default-homestay.jpg';
            }

            imgContainer.appendChild(img);

            // Name text
            const nameText = document.createElement('div');
            nameText.className = 'font-medium text-gray-900';
            nameText.textContent = homestay.name || 'N/A';

            flexContainer.appendChild(imgContainer);
            flexContainer.appendChild(nameText);
            nameCell.appendChild(flexContainer);

            // Location cell
            const locationCell = document.createElement('td');
            locationCell.className = 'px-6 py-4';
            locationCell.textContent = homestay.location || 'N/A';

            // Price cell
            const priceCell = document.createElement('td');
            priceCell.className = 'px-6 py-4';
            const formattedPrice = homestay.price ?
                new Intl.NumberFormat('vi-VN').format(homestay.price) + ' VNĐ' : 'N/A';
            priceCell.textContent = formattedPrice;

            // Capacity cell
            const capacityCell = document.createElement('td');
            capacityCell.className = 'px-6 py-4';
            capacityCell.textContent = homestay.capacity ? `${homestay.capacity} người` : 'N/A';

            // Rating cell
            const ratingCell = document.createElement('td');
            ratingCell.className = 'px-6 py-4';
            const rating = homestay.rating || 0;
            ratingCell.innerHTML = `
                <div class="flex items-center">
                    <span class="text-yellow-400 mr-1">
                        <i class="fas fa-star"></i>
                    </span>
                    <span>${rating.toFixed(1)}</span>
                </div>
            `;

            // Actions cell
            const actionsCell = document.createElement('td');
            actionsCell.className = 'px-6 py-4';
            actionsCell.innerHTML = `
                <div class="flex space-x-3">
                    <a href="/homestay/${homestay.id}" class="text-blue-600 hover:text-blue-900" title="Xem chi tiết" target="_blank">
                        <i class="fas fa-eye"></i>
                    </a>
                    <button onclick="openEditModal(${homestay.id})" class="text-indigo-600 hover:text-indigo-900" title="Chỉnh sửa">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="showDeleteModal(${homestay.id}, '${escapeJsString(homestay.name)}')" class="text-red-600 hover:text-red-900" title="Xóa">
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

    // Function to escape single quotes, backslashes, and newlines for JS strings used in HTML attributes
    function escapeJsString(str) {
        if (!str) return '';
        return str.replace(/\\/g, '\\\\') // Escape backslashes
            .replace(/'/g, "\\'")   // Escape single quotes
            .replace(/\n/g, '\\n') // Escape newlines
            .replace(/\r/g, '\\r'); // Escape carriage returns
    }

    // --- Modal Handling --- 
    window.openAddModal = () => {
        currentHomestayId = null;
        document.getElementById('modalTitle').innerText = 'Thêm Homestay mới';
        document.getElementById('homestayForm').reset(); // Clear form fields
        document.getElementById('homestayForm').action = '/admin/homestay/api/create'; // Set correct action for ADD
        document.getElementById('homestayId').value = ''; // Clear hidden ID

        // Clear image previews
        document.getElementById('imagePreviewContainer').innerHTML = '<span id="imageUploadPlaceholder" class="text-xs text-gray-400">Xem trước ảnh ở đây</span>';
        document.getElementById('existingImagesSection').classList.add('hidden');
        document.getElementById('existingImagePreviewContainer').innerHTML = ''; // Clear existing images too

        document.getElementById('homestayModal').classList.remove('hidden');
    };

    window.openEditModal = async (id) => {
        currentHomestayId = id;
        document.getElementById('modalTitle').innerText = 'Chỉnh sửa Homestay';
        document.getElementById('homestayForm').reset(); // Clear previous data
        document.getElementById('homestayForm').action = `/admin/homestay/api/update`; // Set correct action for EDIT
        document.getElementById('homestayId').value = id;

        // Clear previous previews before loading new data
        document.getElementById('imagePreviewContainer').innerHTML = '<span id="imageUploadPlaceholder" class="text-xs text-gray-400">Xem trước ảnh ở đây</span>';
        document.getElementById('existingImagePreviewContainer').innerHTML = '';
        document.getElementById('existingImagesSection').classList.add('hidden');

        try {
            // Fetch homestay details including images
            const response = await fetch(`/admin/homestay/api/get/${id}`);
            if (!response.ok) throw new Error('Failed to fetch homestay details');
            const data = await response.json();

            if (!data.success) {
                throw new Error(data.message || 'Failed to fetch homestay details');
            }

            const homestay = data.data;

            // Populate form fields
            document.getElementById('name').value = homestay.name || '';
            document.getElementById('location').value = homestay.location || '';
            document.getElementById('description').value = homestay.description || '';
            document.getElementById('price').value = homestay.price || '';
            document.getElementById('capacity').value = homestay.capacity || '';
            // Join amenities array into a comma-separated string for the input field
            document.getElementById('amenities').value = Array.isArray(homestay.amenities) ?
                homestay.amenities.join(', ') : (homestay.amenities || '');

            // Display existing images
            const existingImagesContainer = document.getElementById('existingImagePreviewContainer');
            const imageBaseUrl = '/homestay_images/'; // Base URL for images

            if (homestay.images && homestay.images.length > 0) {
                document.getElementById('existingImagesSection').classList.remove('hidden');
                homestay.images.forEach(imageName => {
                    const imageUrl = imageBaseUrl + imageName;
                    const imgContainer = document.createElement('div');
                    imgContainer.classList.add('relative', 'group'); // Add group for hover effect
                    imgContainer.innerHTML = `
                        <img src="${imageUrl}" alt="Existing Homestay Image" class="w-full h-20 object-cover rounded-md border border-gray-300">
                        <button type="button" onclick="removeExistingImage(this, '${imageName}')" 
                                class="absolute top-0 right-0 bg-red-600 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs opacity-0 group-hover:opacity-100 transition-opacity duration-200" 
                                title="Xóa ảnh này">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                    existingImagesContainer.appendChild(imgContainer);
                });
            }

            document.getElementById('homestayModal').classList.remove('hidden');
        } catch (error) {
            console.error('Error fetching homestay details:', error);
            showErrorAlert('Không thể tải thông tin homestay để chỉnh sửa.');
            closeModal(); // Close modal if fetching fails
        }
    };

    window.closeModal = () => {
        document.getElementById('homestayModal').classList.add('hidden');
        document.getElementById('homestayForm').reset(); // Reset form on close
        document.getElementById('imagePreviewContainer').innerHTML = '<span id="imageUploadPlaceholder" class="text-xs text-gray-400">Xem trước ảnh ở đây</span>'; // Reset preview
        document.getElementById('existingImagesSection').classList.add('hidden');
        document.getElementById('existingImagePreviewContainer').innerHTML = ''; // Clear existing images
    };

    window.openDeleteModal = (id, name) => {
        currentHomestayId = id;
        // Display the name in the confirmation message
        document.getElementById('homestayNameToDelete').innerText = name || 'này';
        document.getElementById('deleteModal').classList.remove('hidden');
    };

    window.closeDeleteModal = () => {
        document.getElementById('deleteModal').classList.add('hidden');
        document.getElementById('homestayNameToDelete').innerText = ''; // Clear name
        currentHomestayId = null;
    };

    window.confirmDelete = async () => {
        if (!currentHomestayId) return;

        const deleteButton = document.getElementById('confirmDeleteButton');
        deleteButton.disabled = true;
        deleteButton.innerHTML = '<i class="fas fa-spinner fa-spin mr-1"></i> Đang xóa...'; // Loading state

        try {
            const response = await fetch(`/admin/homestay/api/delete/${currentHomestayId}`, {
                method: 'DELETE',
                headers: {
                    [csrfHeader]: csrfToken
                }
            });

            if (!response.ok) {
                // Try to read error message from response body
                let errorMsg = 'Xóa homestay thất bại.';
                try {
                    const errorData = await response.json();
                    errorMsg = errorData.message || errorMsg;
                } catch (e) { /* Ignore if body isn't JSON */ }
                throw new Error(errorMsg);
            }

            showSuccessAlert('Xóa homestay thành công!');
            fetchHomestays(); // Refresh the list
        } catch (error) {
            console.error('Error deleting homestay:', error);
            showErrorAlert(error.message || 'Lỗi không xác định khi xóa homestay.');
        } finally {
            closeDeleteModal();
            deleteButton.disabled = false;
            deleteButton.innerHTML = 'Xóa'; // Reset button text
        }
    };

    // Handle form submission for both Add and Edit
    document.getElementById('homestayForm').addEventListener('submit', async (event) => {
        event.preventDefault();
        const form = event.target;
        const formData = new FormData(form);

        // Add homestayId for updates
        if (currentHomestayId) {
            formData.set('homestayId', currentHomestayId);
        }

        // Track if we're in edit mode
        const isEditMode = currentHomestayId != null;
        const url = isEditMode ? '/admin/homestay/api/update' : '/admin/homestay/api/create';

        const saveButton = document.getElementById('saveButton');
        saveButton.disabled = true;
        saveButton.innerHTML = '<i class="fas fa-spinner fa-spin mr-1"></i> Đang lưu...'; // Loading state

        // Convert amenities string to comma-separated if needed
        const amenitiesValue = formData.get('amenities');
        if (amenitiesValue) {
            // Clean up amenities - remove extra spaces around commas
            const cleanedAmenities = amenitiesValue.split(',')
                .map(s => s.trim())
                .filter(s => s)
                .join(',');
            formData.set('amenities', cleanedAmenities);
        }

        // For edit mode, handle the current images JSON
        if (isEditMode) {
            // Collect existing image URLs into a JSON array
            const existingImagesContainer = document.getElementById('existingImagePreviewContainer');
            const currentImages = [];

            if (existingImagesContainer) {
                const imgElements = existingImagesContainer.querySelectorAll('img');
                imgElements.forEach(img => {
                    if (img.src) {
                        currentImages.push(img.src);
                    }
                });
            }

            // Add to form data as JSON string
            formData.set('currentImages', JSON.stringify(currentImages));
        }

        // Handle file input separately
        const filesInput = document.getElementById('newImages');
        if (filesInput.files.length > 0) {
            // In edit mode, append as newImages
            if (isEditMode) {
                // Clear existing files and re-add them to ensure proper naming
                formData.delete('newImages');
                for (let i = 0; i < filesInput.files.length; i++) {
                    formData.append('newImages', filesInput.files[i]);
                }
            }
            // In create mode, append as images
            else {
                formData.delete('newImages');
                for (let i = 0; i < filesInput.files.length; i++) {
                    formData.append('images', filesInput.files[i]);
                }
            }
        }

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    [csrfHeader]: csrfToken
                },
                body: formData
            });

            if (!response.ok) {
                let errorMsg = isEditMode ? 'Cập nhật homestay thất bại.' : 'Thêm homestay thất bại.';
                try {
                    const errorData = await response.json();
                    errorMsg = errorData.message || errorMsg;
                } catch (e) { /* Ignore if response is not JSON */ }
                throw new Error(errorMsg);
            }

            const result = await response.json();
            if (!result.success) {
                throw new Error(result.message || 'Thao tác không thành công.');
            }

            showSuccessAlert(isEditMode ? 'Cập nhật homestay thành công!' : 'Thêm homestay thành công!');
            closeModal();
            fetchHomestays(); // Refresh list
        } catch (error) {
            console.error('Error saving homestay:', error);
            showErrorAlert(error.message || 'Lỗi không xác định khi lưu homestay.');
        } finally {
            saveButton.disabled = false;
            saveButton.innerHTML = '<i class="fas fa-save mr-1"></i> Lưu'; // Reset button
        }
    });

    // --- Image Preview and Handling --- 

    window.previewNewImages = (event) => {
        const previewContainer = document.getElementById('imagePreviewContainer');
        const placeholder = document.getElementById('imageUploadPlaceholder');
        const files = event.target.files;
        const existingPreviews = previewContainer.querySelectorAll('.new-image-preview').length;
        const existingServerImagesCount = document.getElementById('existingImagePreviewContainer').children.length;

        // Clear only *new* image previews before adding new ones
        previewContainer.querySelectorAll('.new-image-preview').forEach(el => el.remove());

        if (files.length === 0) { // If no files selected, ensure placeholder shows if container empty
            if(previewContainer.children.length === 1 && previewContainer.firstChild.id === 'imageUploadPlaceholder') {
                placeholder.classList.remove('hidden');
            }
            return;
        }

        placeholder.classList.add('hidden'); // Hide placeholder when adding previews

        let filesAdded = 0;
        for (let i = 0; i < files.length; i++) {
            // Check total image count limit (existing server + already previewed + current file)
            if (existingServerImagesCount + existingPreviews + filesAdded >= maxImages) {
                showErrorAlert(`Bạn chỉ có thể tải lên tối đa ${maxImages} ảnh.`);
                // Clear the file input to prevent inconsistent state
                event.target.value = null;
                // Re-clear previews and show placeholder if needed
                previewContainer.querySelectorAll('.new-image-preview').forEach(el => el.remove());
                if(previewContainer.children.length === 1 && previewContainer.firstChild.id === 'imageUploadPlaceholder') {
                    placeholder.classList.remove('hidden');
                }
                return;
            }

            const file = files[i];
            // Basic file type check (optional, but good practice)
            if (!file.type.startsWith('image/')) {
                showErrorAlert(`File ${file.name} không phải là ảnh.`);
                continue; // Skip this file
            }
            // Basic file size check (e.g., 10MB)
            if (file.size > 10 * 1024 * 1024) {
                showErrorAlert(`Ảnh ${file.name} quá lớn (tối đa 10MB).`);
                continue; // Skip this file
            }

            const reader = new FileReader();
            reader.onload = function(e) {
                const imgContainer = document.createElement('div');
                imgContainer.classList.add('relative', 'group', 'new-image-preview'); // Add group and specific class
                imgContainer.innerHTML = `
                    <img src="${e.target.result}" alt="Preview Image" class="w-full h-20 object-cover rounded-md border border-gray-300">
                    <button type="button" onclick="removeNewImagePreview(this)" 
                            class="absolute top-0 right-0 bg-red-600 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs opacity-0 group-hover:opacity-100 transition-opacity duration-200" 
                            title="Gỡ ảnh này">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                // Store the File object with the preview element for later removal from input
                imgContainer._fileRef = file;
                previewContainer.appendChild(imgContainer);
            }
            reader.readAsDataURL(file);
            filesAdded++;
        }

        // Re-check placeholder visibility after potential skips
        if(previewContainer.querySelectorAll('.new-image-preview').length === 0) {
            placeholder.classList.remove('hidden');
        }
    };

    // Function to remove a *new* image preview
    window.removeNewImagePreview = (button) => {
        const imgContainer = button.closest('.new-image-preview');
        const fileToRemove = imgContainer._fileRef; // Get the File object stored earlier
        imgContainer.remove();

        // Remove the corresponding file from the FileList in the input
        const input = document.getElementById('newImages');
        const dataTransfer = new DataTransfer();
        const files = Array.from(input.files);

        files.forEach(file => {
            if (file !== fileToRemove) {
                dataTransfer.items.add(file);
            }
        });
        input.files = dataTransfer.files; // Assign the updated FileList

        // Show placeholder if no previews left
        const previewContainer = document.getElementById('imagePreviewContainer');
        const placeholder = document.getElementById('imageUploadPlaceholder');
        if(previewContainer.querySelectorAll('.new-image-preview').length === 0) {
            placeholder.classList.remove('hidden');
        }
    };

    // Function to handle removal of an *existing* image (from server)
    window.removeExistingImage = async (button, imageName) => {
        if (!currentHomestayId || !imageName) {
            showErrorAlert('Không thể xác định ảnh cần xóa.');
            return;
        }

        // Confirmation (optional but recommended)
        if (!confirm(`Bạn có chắc chắn muốn xóa ảnh ${imageName} khỏi homestay này? Hành động này sẽ xóa file vĩnh viễn.`)) {
            return;
        }

        const imgContainer = button.closest('div.relative'); // Get the container of the image and button
        imgContainer.style.opacity = '0.5'; // Visually indicate removal attempt
        button.disabled = true;

        try {
            const response = await fetch(`/admin/homestay/${currentHomestayId}/images/${imageName}`, { // Specific endpoint for deleting one image
                method: 'DELETE',
                headers: {
                    [csrfHeader]: csrfToken
                }
            });

            if (!response.ok) {
                let errorMsg = 'Xóa ảnh thất bại.';
                try { const errorData = await response.json(); errorMsg = errorData.message || errorMsg; } catch(e) {}
                throw new Error(errorMsg);
            }

            // If successful, remove the preview element from the DOM
            imgContainer.remove();
            showSuccessAlert(`Đã xóa ảnh ${imageName}.`);

            // Hide the 'Existing Images' section if no images remain
            const existingImagesContainer = document.getElementById('existingImagePreviewContainer');
            if (existingImagesContainer.children.length === 0) {
                document.getElementById('existingImagesSection').classList.add('hidden');
            }

        } catch (error) {
            console.error('Error deleting existing image:', error);
            showErrorAlert(error.message);
            // Restore visual state if deletion failed
            imgContainer.style.opacity = '1';
            button.disabled = false;
        }
    };

    // --- Alert Handling ---
    function showErrorAlert(message) {
        document.getElementById('errorMessage').textContent = message;
        document.getElementById('errorAlert').classList.remove('hidden');
        // Auto-hide after 5 seconds
        setTimeout(hideErrorAlert, 5000);
    }

    function showSuccessAlert(message) {
        document.getElementById('successMessage').textContent = message;
        document.getElementById('successAlert').classList.remove('hidden');
        // Auto-hide after 3 seconds
        setTimeout(hideSuccessAlert, 3000);
    }

    window.hideErrorAlert = () => {
        document.getElementById('errorAlert').classList.add('hidden');
    };

    window.hideSuccessAlert = () => {
        document.getElementById('successAlert').classList.add('hidden');
    };

    // Thêm các hàm hiển thị và ẩn loading
    function showLoading() {
        document.getElementById('loadingTable').classList.remove('hidden');
        document.getElementById('homestayTableContainer').classList.add('hidden');
        document.getElementById('noResults').classList.add('hidden');
    }

    function hideLoading() {
        document.getElementById('loadingTable').classList.add('hidden');
    }
}); 