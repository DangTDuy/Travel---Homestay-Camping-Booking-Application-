package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ut.edu.project.models.Camping;
import ut.edu.project.models.User;
import ut.edu.project.models.Booking;
import ut.edu.project.repositories.CampingRepository;
import ut.edu.project.repositories.UserBehaviorRepository;
import ut.edu.project.repositories.ReviewRepository;
import ut.edu.project.repositories.BookingRepository;
import ut.edu.project.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.io.File;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

@Service
@Slf4j
@Transactional
public class CampingService {
    @Autowired
    private CampingRepository campingRepository; // Repository quản lý khu cắm trại

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserBehaviorRepository userBehaviorRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Value("${app.upload.dir.camping:project/src/main/resources/static/camping_images}")
    private String UPLOAD_DIR;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            // Initialize rootLocation
            rootLocation = Paths.get(UPLOAD_DIR);

            // Create upload directory if it doesn't exist
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    // Tạo khu cắm trại mới
    @Transactional
    public Camping createCamping(Camping camping, String username) {
        try {
            // Validate user
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getRole().equals("ADMIN")) {
                throw new RuntimeException("Bạn không có quyền tạo khu cắm trại");
            }

            // Basic validation
            validateCamping(camping);

            // Set owner and initialize lists
            camping.setOwner(user);
            if (camping.getImages() == null) camping.setImages(new ArrayList<>());
            if (camping.getImageUrls() == null) camping.setImageUrls(new ArrayList<>());
            if (camping.getFacilities() == null) camping.setFacilities(new ArrayList<>());
            if (camping.getEquipment() == null) camping.setEquipment(new ArrayList<>());
            if (camping.getRules() == null) camping.setRules(new ArrayList<>());

            // Save camping first to get ID
            Camping savedCamping = campingRepository.saveAndFlush(camping);
            return savedCamping;
        } catch (Exception e) {
            log.error("Error creating camping: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tạo khu cắm trại: " + e.getMessage(), e);
        }
    }

    // Cập nhật thông tin khu cắm trại
    @Transactional
    public Camping updateCamping(Long id, Camping camping) {
        Camping existingCamping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại theo ID
        existingCamping.setName(camping.getName()); // Cập nhật tên
        existingCamping.setLocation(camping.getLocation()); // Cập nhật địa điểm
        existingCamping.setDescription(camping.getDescription()); // Cập nhật mô tả
        existingCamping.setPrice(camping.getPrice()); // Cập nhật giá
        existingCamping.setCapacity(camping.getCapacity()); // Cập nhật sức chứa tối đa
        existingCamping.setAvailableSlots(camping.getAvailableSlots()); // Cập nhật số chỗ có sẵn
        existingCamping.setIsAvailable(camping.isAvailable()); // Cập nhật trạng thái sẵn sàng
        existingCamping.setImageUrls(camping.getImageUrls()); // Cập nhật danh sách URL ảnh
        existingCamping.setFacilities(camping.getFacilities()); // Cập nhật danh sách tiện ích
        existingCamping.setRules(camping.getRules()); // Cập nhật quy tắc
        existingCamping.setTerrain(camping.getTerrain()); // Cập nhật địa hình
        existingCamping.setWeather(camping.getWeather()); // Cập nhật thời tiết
        existingCamping.setEquipment(camping.getEquipment()); // Cập nhật thiết bị
        validateCamping(existingCamping); // Kiểm tra tính hợp lệ sau khi cập nhật
        return campingRepository.save(existingCamping); // Lưu thay đổi
    }

    // Lấy thông tin khu cắm trại theo ID
    @Transactional
    public Optional<Camping> getCampingById(Long id) {
        try {
            Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khu cắm trại không tìm thấy"));

            // Initialize lazy-loaded collections
            Hibernate.initialize(camping.getImages());
            Hibernate.initialize(camping.getImageUrls());
            Hibernate.initialize(camping.getFacilities());
            Hibernate.initialize(camping.getEquipment());
            Hibernate.initialize(camping.getRules());
            Hibernate.initialize(camping.getReviews());

            // Initialize owner
            Hibernate.initialize(camping.getOwner());

            return Optional.of(camping);
        } catch (Exception e) {
            log.error("Error getting camping: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    // Lấy tất cả khu cắm trại
    @Transactional(readOnly = true)
    public List<Camping> getAllCampings() {
        try {
            List<Camping> campings = campingRepository.findAll();
            log.info("Retrieved {} campings", campings.size());
            return campings;
        } catch (Exception e) {
            log.error("Error retrieving all campings: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy danh sách khu cắm trại: " + e.getMessage());
        }
    }

    // Xóa khu cắm trại
    @Transactional
    public void deleteCamping(Long id) {
        try {
            Camping camping = campingRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Khu cắm trại không tồn tại"));

            // Xóa ảnh liên quan
            if (camping.getImages() != null && !camping.getImages().isEmpty()) {
                for (String image : camping.getImages()) {
                    deleteImage(image);
                }
            }

            // Xóa khu cắm trại
            campingRepository.deleteById(id);
            log.info("Deleted camping with ID: {}", id);
        } catch (IllegalArgumentException e) {
            log.error("Cannot delete camping: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error deleting camping: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xóa khu cắm trại: " + e.getMessage());
        }
    }

    // Kiểm tra tính hợp lệ của khu cắm trại
    private void validateCamping(Camping camping) {
        if (camping == null) {
            throw new IllegalArgumentException("Thông tin khu cắm trại không được để trống");
        }

        if (camping.getName() == null || camping.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khu cắm trại không được để trống");
        }

        if (camping.getLocation() == null || camping.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Địa điểm không được để trống");
        }

        if (camping.getDescription() == null || camping.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Mô tả không được để trống");
        }

        if (camping.getPrice() == null || camping.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá phải lớn hơn 0");
        }

        if (camping.getCapacity() == null || camping.getCapacity() <= 0) {
            throw new IllegalArgumentException("Sức chứa phải lớn hơn 0");
        }

        if (camping.getOwner() == null) {
            throw new IllegalArgumentException("Chủ sở hữu không được để trống");
        }
    }

    // Đặt chỗ khu cắm trại
    public boolean bookCamping(Long id, LocalDate startDate, LocalDate endDate, int numberOfPeople, String customerName) {
        return campingRepository.findById(id).map(camping -> {
            if (camping.isAvailable() && numberOfPeople <= camping.getCapacity() && isAvailableForDates(camping, startDate, endDate)) {
                Booking booking = new Booking(); // Tạo đặt chỗ mới
                booking.setCheckIn(startDate.atStartOfDay()); // Ngày nhận chỗ
                booking.setCheckOut(endDate.atStartOfDay()); // Ngày trả chỗ
                booking.setGuests(numberOfPeople); // Số lượng khách
                booking.setStatus(Booking.BookingStatus.PENDING); // Trạng thái chờ xử lý
                camping.addBooking(booking); // Thêm đặt chỗ vào khu cắm trại
                campingRepository.save(camping); // Lưu thay đổi
                return true;
            }
            return false;
        }).orElse(false); // Trả về false nếu không tìm thấy khu cắm trại
    }

    // Hủy đặt chỗ khu cắm trại
    public boolean cancelBooking(Long id, int bookingIndex) {
        return campingRepository.findById(id).map(camping -> {
            List<Booking> bookings = camping.getBookings();
            if (bookingIndex >= 0 && bookingIndex < bookings.size()) {
                Booking booking = bookings.get(bookingIndex); // Lấy đặt chỗ theo chỉ số
                if (booking.getCheckIn().toLocalDate().isAfter(LocalDate.now().plusDays(1))) {
                    booking.setStatus(Booking.BookingStatus.CANCELLED); // Đặt trạng thái hủy
                    campingRepository.save(camping); // Lưu thay đổi
                    return true;
                }
            }
            return false;
        }).orElse(false); // Trả về false nếu không tìm thấy khu cắm trại
    }

    // Tìm kiếm khu cắm trại theo tên với phân trang
    public Page<Camping> searchCampingsByName(String name, int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort); // Tạo yêu cầu phân trang
        return campingRepository.findByNameContainingIgnoreCase(name, pageRequest); // Trả về trang kết quả
    }

    // Lấy khu cắm trại theo số chỗ tối thiểu với phân trang
    public Page<Camping> getCampingsByMinPlaces(int minPlaces, int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort); // Tạo yêu cầu phân trang
        return campingRepository.findByCapacityGreaterThanEqual(minPlaces, pageRequest); // Trả về trang kết quả
    }

    // Lấy danh sách khu cắm trại với phân trang và lọc theo ngày
    public Page<Camping> getCampingsPaginated(int page, int size, String sort, LocalDate startDate, LocalDate endDate) {
        PageRequest pageRequest = buildPageRequest(page, size, sort); // Tạo yêu cầu phân trang
        Page<Camping> campingPage = campingRepository.findAll(pageRequest); // Lấy tất cả khu cắm trại
        if (startDate != null && endDate != null) {
            List<Camping> filteredList = campingPage.getContent().stream()
                    .filter(c -> isAvailableForDates(c, startDate, endDate)) // Lọc theo ngày sẵn sàng
                    .collect(Collectors.toList());
            return new PageImpl<>(filteredList, pageRequest, campingPage.getTotalElements()); // Trả về trang đã lọc
        }
        return campingPage; // Trả về trang gốc nếu không có lọc ngày
    }

    // Kiểm tra tính sẵn sàng của khu cắm trại trong khoảng thời gian
    private boolean isAvailableForDates(Camping camping, LocalDate start, LocalDate end) {
        for (Booking booking : camping.getBookings()) {
            if (booking.getStatus() != Booking.BookingStatus.CANCELLED &&
                    !(end.isBefore(booking.getCheckIn().toLocalDate()) ||
                            start.isAfter(booking.getCheckOut().toLocalDate()))) {
                return false; // Không sẵn sàng nếu có đặt chỗ trùng
            }
        }
        return true; // Sẵn sàng nếu không có trùng lặp
    }

    // Xây dựng yêu cầu phân trang và sắp xếp
    private PageRequest buildPageRequest(int page, int size, String sort) {
        if (sort == null || sort.isEmpty()) {
            return PageRequest.of(page, size); // Không sắp xếp nếu sort rỗng
        }
        switch (sort) {
            case "price_asc":
                return PageRequest.of(page, size, Sort.by("price").ascending()); // Sắp xếp giá tăng dần
            case "price_desc":
                return PageRequest.of(page, size, Sort.by("price").descending()); // Sắp xếp giá giảm dần
            case "capacity_asc":
                return PageRequest.of(page, size, Sort.by("capacity").ascending()); // Sắp xếp sức chứa tăng dần
            case "capacity_desc":
                return PageRequest.of(page, size, Sort.by("capacity").descending()); // Sắp xếp sức chứa giảm dần
            default:
                return PageRequest.of(page, size); // Mặc định không sắp xếp
        }
    }

    // Thêm đánh giá mới
    @Transactional
    public Camping addReview(Long id, Integer rating, String comment, String reply) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Điểm đánh giá phải từ 1 đến 5"); // Kiểm tra điểm hợp lệ
        }
        camping.addReview(comment, rating, reply); // Thêm đánh giá
        return campingRepository.save(camping); // Lưu thay đổi
    }

    // Phản hồi một đánh giá
    @Transactional
    public Camping replyToReview(Long id, int reviewIndex, String reply) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại
        camping.replyToReview(reviewIndex, reply); // Thêm phản hồi
        return campingRepository.save(camping); // Lưu thay đổi
    }

    // Thêm thông báo mới
    @Transactional
    public Camping addNotification(Long id, String message) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại
        camping.addNotification(message); // Thêm thông báo
        return campingRepository.save(camping); // Lưu thay đổi
    }

    // Lấy danh sách thông báo
    public List<String> getNotifications(Long id) {
        return campingRepository.findById(id)
                .map(camping -> camping.getRecentNotifications(5)) // Lấy 5 thông báo gần nhất
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại"));
    }

    // Thêm từ khóa tìm kiếm
    @Transactional
    public Camping addSearchTerm(Long id, String term) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại
        camping.addSearchTerm(term); // Thêm từ khóa
        return campingRepository.save(camping); // Lưu thay đổi
    }

    // Lấy danh sách khu cắm trại gợi ý
    public List<Camping> getSuggestedCampings(String term) {
        return campingRepository.findBySearchTerm(term); // Tìm khu cắm trại theo từ khóa
    }

    // Xử lý thanh toán
    public String processPayment(Long id, Double amount) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại")); // Tìm khu cắm trại
        if (amount < camping.getPrice()) {
            camping.setPaymentStatus("FAILED: Số tiền không đủ"); // Cập nhật trạng thái thất bại
            campingRepository.save(camping); // Lưu thay đổi
            return "Thanh toán thất bại: Số tiền nhỏ hơn giá";
        }
        camping.setPaymentStatus("SUCCESS"); // Cập nhật trạng thái thành công
        campingRepository.save(camping); // Lưu thay đổi
        return "Thanh toán thành công";
    }

    @Transactional
    public synchronized void uploadImages(Long campingId, MultipartFile[] images) throws IOException {
        try {
            Camping camping = campingRepository.findById(campingId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khu cắm trại"));

            for (MultipartFile file : images) {
                if (file.isEmpty()) {
                    continue;
                }

                validateImage(file);

                // Tạo tên file duy nhất
                String fileExtension = getFileExtension(file.getOriginalFilename());
                String newFileName = "camping_" + campingId + "_" + System.currentTimeMillis() + fileExtension;

                // Lưu file
                Path targetLocation = rootLocation.resolve(newFileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                // Cập nhật danh sách ảnh
                camping.getImages().add(newFileName);
                camping.getImageUrls().add("/camping_images/" + newFileName);
            }

            // Lưu cập nhật vào database
            campingRepository.save(camping);
        } catch (IOException e) {
            log.error("Error uploading images: {}", e.getMessage(), e);
            throw new IOException("Lỗi khi tải lên hình ảnh: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error during image upload: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xử lý tải lên hình ảnh: " + e.getMessage());
        }
    }

    @Transactional
    public Camping updateCampingImages(Camping camping, List<String> currentImages, MultipartFile[] newImages) throws IOException {
        try {
            // Xóa các ảnh không còn trong danh sách mới
            List<String> oldImageUrls = new ArrayList<>(camping.getImageUrls());
            List<String> oldImages = new ArrayList<>(camping.getImages());

            // Xóa ảnh cũ không còn trong danh sách hiện tại
            for (int i = 0; i < oldImageUrls.size(); i++) {
                String oldImageUrl = oldImageUrls.get(i);
                if (!currentImages.contains(oldImageUrl)) {
                    // Xóa file ảnh
                    String oldImage = oldImages.get(i);
                    deleteImage(oldImage);
                    
                    // Xóa khỏi danh sách
                    camping.getImageUrls().remove(oldImageUrl);
                    camping.getImages().remove(oldImage);
                }
            }

            // Lưu cập nhật
            Camping updatedCamping = campingRepository.save(camping);

            // Thêm ảnh mới nếu có
            if (newImages != null && newImages.length > 0) {
                uploadImages(camping.getId(), newImages);
                // Lấy lại camping sau khi upload ảnh mới
                updatedCamping = campingRepository.findById(camping.getId()).orElse(camping);
            }

            return updatedCamping;
        } catch (Exception e) {
            log.error("Error updating camping images: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi cập nhật hình ảnh khu cắm trại: " + e.getMessage());
        }
    }
    
    @Transactional
    public void deleteImages(Long campingId, List<String> imageUrls) {
        try {
            Camping camping = campingRepository.findById(campingId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khu cắm trại"));

            for (String imageUrl : imageUrls) {
                if (camping.getImageUrls().contains(imageUrl)) {
                    int index = camping.getImageUrls().indexOf(imageUrl);
                    if (index >= 0 && index < camping.getImages().size()) {
                        String imageName = camping.getImages().get(index);
                        deleteImage(imageName);
                        camping.getImageUrls().remove(imageUrl);
                        camping.getImages().remove(imageName);
                    }
                }
            }

            campingRepository.save(camping);
        } catch (Exception e) {
            log.error("Error deleting images: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xóa hình ảnh: " + e.getMessage());
        }
    }

    private void deleteImage(String filename) {
        try {
            Path filePath = rootLocation.resolve(filename);
            Files.deleteIfExists(filePath);
            log.info("Deleted image: {}", filename);
        } catch (IOException e) {
            log.error("Error deleting image file: {}", filename, e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        // Check file size (e.g., limit to 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Additional validation for common image formats
        String[] allowedTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"};
        boolean isAllowed = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("Only JPEG, PNG, GIF, and WEBP images are allowed");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public List<Camping> searchCampings(String location, Double minPrice, Double maxPrice) {
        try {
            log.info("Searching campings with location: {}, minPrice: {}, maxPrice: {}",
                    location, minPrice, maxPrice);

            // If no search parameters, return all campings
            if ((location == null || location.isEmpty()) &&
                    minPrice == null &&
                    maxPrice == null) {
                log.info("No search parameters, returning all campings");
                return getAllCampings();
            }

            log.info("Searching with parameters - location: {}, minPrice: {}, maxPrice: {}",
                    location, minPrice, maxPrice);

            List<Camping> results = campingRepository.searchCampings(
                    location,
                    minPrice,
                    maxPrice
            );

            log.info("Found {} campings matching search criteria", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error searching campings: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tìm kiếm khu cắm trại: " + e.getMessage());
        }
    }

    public List<Camping> getCampingsByOwner(String username) {
        User owner = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return campingRepository.findByOwner(owner);
    }
}