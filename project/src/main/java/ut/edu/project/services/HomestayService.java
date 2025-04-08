package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;
import ut.edu.project.repositories.HomestayRepository;
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
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.io.File;
import java.util.stream.Collectors;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

@Service
@Slf4j
@Transactional
public class HomestayService {

    @Autowired
    private HomestayRepository homestayRepository;

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

    private final String UPLOAD_DIR = "src/main/resources/static/homestay_images";
    private final Path rootLocation = Paths.get(UPLOAD_DIR);

    @PostConstruct
    public void init() {
        try {
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

    @Transactional
    public Homestay createHomestay(Homestay homestay, String username) {
        try {
            // Validate user
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getRole().equals("ADMIN")) {
                throw new RuntimeException("Bạn không có quyền tạo homestay");
            }

            // Basic validation
            validateHomestay(homestay);

            // Set owner and initialize lists
            homestay.setOwner(user);
            if (homestay.getImages() == null) homestay.setImages(new ArrayList<>());
            if (homestay.getImageUrls() == null) homestay.setImageUrls(new ArrayList<>());
            if (homestay.getAmenities() == null) homestay.setAmenities(new ArrayList<>());

            // Save homestay first to get ID
            Homestay savedHomestay = homestayRepository.saveAndFlush(homestay);
            return savedHomestay;
        } catch (Exception e) {
            log.error("Error creating homestay: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tạo homestay: " + e.getMessage(), e);
        }
    }

    public List<Homestay> searchHomestays(String location, String priceRange, String rating) {
        try {
            log.info("Searching homestays with location: {}, priceRange: {}, rating: {}",
                    location, priceRange, rating);

            // If no search parameters, return all homestays
            if ((location == null || location.isEmpty()) &&
                    (priceRange == null || priceRange.isEmpty()) &&
                    (rating == null || rating.isEmpty())) {
                log.info("No search parameters, returning all homestays");
                return getAllHomestays();
            }

            Double minPrice = null;
            Double maxPrice = null;
            Double minRating = null;

            // Parse price range
            if (priceRange != null && !priceRange.isEmpty()) {
                String[] prices = priceRange.split("-");

                // Trường hợp "X-" - từ X trở lên
                if (prices.length == 2) {
                    try {
                        // Lấy giá trị min nếu có
                        if (!prices[0].isEmpty()) {
                            minPrice = Double.parseDouble(prices[0]);
                            log.info("Min price set to: {}", minPrice);
                        }

                        // Lấy giá trị max nếu có
                        if (!prices[1].isEmpty()) {
                            maxPrice = Double.parseDouble(prices[1]);
                            log.info("Max price set to: {}", maxPrice);
                        }
                        // Trường hợp "X-" chỉ có min, không có max
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse price range: {}", priceRange, e);
                    }
                }
                // Trường hợp chỉ có một giá trị
                else if (prices.length == 1 && !prices[0].isEmpty()) {
                    try {
                        double price = Double.parseDouble(prices[0]);
                        minPrice = price;
                        maxPrice = price;
                        log.info("Single price value: {}", price);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid price format: {}", prices[0], e);
                    }
                }
            }

            // Parse rating
            if (rating != null && !rating.isEmpty()) {
                try {
                    minRating = Double.parseDouble(rating);
                } catch (NumberFormatException e) {
                    log.warn("Invalid rating format: {}", rating, e);
                }
            }

            log.info("Searching with parameters - location: {}, minPrice: {}, maxPrice: {}, minRating: {}",
                    location, minPrice, maxPrice, minRating);

            List<Homestay> results = homestayRepository.searchHomestays(
                    location,
                    minPrice,
                    maxPrice,
                    minRating
            );

            log.info("Found {} homestays matching search criteria", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error searching homestays: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tìm kiếm homestay: " + e.getMessage());
        }
    }

    public Optional<Homestay> getHomestayById(Long id) {
        try {
            log.info("Getting homestay by id: {}", id);
            Optional<Homestay> homestayOpt = homestayRepository.findById(id);

            // Initialize lazy collections needed by the detail view
            homestayOpt.ifPresent(homestay -> {
                Hibernate.initialize(homestay.getReviews());
                Hibernate.initialize(homestay.getImages()); // Initialize images as well if needed elsewhere
                Hibernate.initialize(homestay.getAmenities()); // Initialize amenities
            });

            log.info("Found homestay: {}", homestayOpt.isPresent());
            return homestayOpt;
        } catch (Exception e) {
            log.error("Error getting homestay by id: {}", id, e);
            throw new RuntimeException("Lỗi khi lấy thông tin homestay: " + e.getMessage());
        }
    }

    public List<Homestay> getHomestaysByOwner(String username) {
        User owner = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return homestayRepository.findByOwner(owner);
    }

    public Homestay updateHomestay(Long id, Homestay updatedHomestay, String username) {
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Homestay not found"));

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().equals("ADMIN")) {
            throw new RuntimeException("Chỉ ADMIN mới có quyền cập nhật homestay");
        }

        homestay.setName(updatedHomestay.getName());
        homestay.setLocation(updatedHomestay.getLocation());
        homestay.setDescription(updatedHomestay.getDescription());
        homestay.setPrice(updatedHomestay.getPrice());
        homestay.setCapacity(updatedHomestay.getCapacity());
        homestay.setAmenities(updatedHomestay.getAmenities());

        // Cập nhật danh sách ảnh
        if (updatedHomestay.getImageUrls() != null) {
            // Xóa các ảnh cũ không còn trong danh sách mới
            if (homestay.getImages() != null) {
                List<String> oldImages = new ArrayList<>(homestay.getImages());
                for (String oldImage : oldImages) {
                    String oldImageUrl = "/homestay_images/" + oldImage;
                    if (!updatedHomestay.getImageUrls().contains(oldImageUrl)) {
                        try {
                            Files.deleteIfExists(rootLocation.resolve(oldImage));
                            homestay.getImages().remove(oldImage);
                        } catch (IOException e) {
                            System.err.println("Could not delete image: " + oldImage);
                        }
                    }
                }
            }
            homestay.setImageUrls(updatedHomestay.getImageUrls());
        }

        return homestayRepository.save(homestay);
    }

    @Transactional
    public void deleteHomestay(Long id) {
        try {
            Homestay homestay = homestayRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Homestay không tồn tại"));

            // Delete all images first
            if (homestay.getImages() != null) {
                for (String image : homestay.getImages()) {
                    deleteImage(image);
                }
            }

            // Clear all collections
            homestay.getImages().clear();
            homestay.getImageUrls().clear();
            homestay.getAmenities().clear();
            homestayRepository.saveAndFlush(homestay);

            // Delete the homestay
            homestayRepository.delete(homestay);
            homestayRepository.flush();
        } catch (Exception e) {
            log.error("Error deleting homestay {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xóa homestay: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Homestay> getAllHomestays() {
        try {
            log.info("Attempting to get all homestays from repository...");
            List<Homestay> homestays = homestayRepository.findAll();
            log.info("Initial fetch from repository found {} homestays.", homestays.size());

            // Đảm bảo danh sách không null
            if (homestays == null) {
                log.warn("Repository returned null list of homestays");
                return new ArrayList<>();
            }

            // Lọc bỏ các homestay null (nếu có)
            List<Homestay> validHomestays = new ArrayList<>();
            
            // Ensure all necessary collections are initialized within the transaction
            for (Homestay homestay : homestays) {
                if (homestay == null) {
                    log.warn("Found null homestay in result list, skipping");
                    continue;
                }
                
                try {
                    // Đảm bảo ID không null
                    if (homestay.getId() == null) {
                        log.warn("Found homestay with null ID, skipping");
                        continue;
                    }
                    
                    // Khởi tạo các collection cần thiết một cách an toàn
                    // Nếu collection là null, tạo mới thay vì bỏ qua
                    if (homestay.getAmenities() == null) {
                        homestay.setAmenities(new ArrayList<>());
                    } else {
                        Hibernate.initialize(homestay.getAmenities());
                    }
                    
                    if (homestay.getImages() == null) {
                        homestay.setImages(new ArrayList<>());
                    } else {
                        Hibernate.initialize(homestay.getImages());
                    }
                    
                    if (homestay.getImageUrls() == null) {
                        homestay.setImageUrls(new ArrayList<>());
                    } else {
                        Hibernate.initialize(homestay.getImageUrls());
                    }
                    
                    // Đảm bảo các trường cơ bản không null
                    if (homestay.getBookingCount() == null) {
                        homestay.setBookingCount(0);
                    }
                    
                    if (homestay.getRating() == null) {
                        homestay.setRating(0.0);
                    }
                    
                    validHomestays.add(homestay);
                } catch (Exception e) {
                    log.error("Error initializing collections for homestay ID {}: {}", 
                            homestay.getId(), e.getMessage());
                    // Thêm homestay vào danh sách ngay cả khi có lỗi để đảm bảo hiển thị đủ
                    try {
                        // Đảm bảo các collection cơ bản không null trước khi thêm vào danh sách
                        if (homestay.getAmenities() == null) homestay.setAmenities(new ArrayList<>());
                        if (homestay.getImages() == null) homestay.setImages(new ArrayList<>());
                        if (homestay.getImageUrls() == null) homestay.setImageUrls(new ArrayList<>());
                        if (homestay.getBookingCount() == null) homestay.setBookingCount(0);
                        if (homestay.getRating() == null) homestay.setRating(0.0);
                        
                        validHomestays.add(homestay);
                    } catch (Exception ex) {
                        log.error("Cannot recover homestay ID {}: {}", homestay.getId(), ex.getMessage());
                    }
                }
            }

            log.info("Returning {} valid homestays.", validHomestays.size());
            return validHomestays;
        } catch (Exception e) {
            log.error("Critical error getting all homestays: {}", e.getMessage(), e);
            // Trả về danh sách rỗng thay vì ném ngoại lệ để tránh lỗi trang
            return new ArrayList<>();
        }
    }

    @Transactional
    public synchronized void uploadImages(Long homestayId, MultipartFile[] images) throws IOException {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 ảnh");
        }

        if (images.length > 5) {
            throw new IllegalArgumentException("Chỉ được phép tải lên tối đa 5 ảnh");
        }

        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new RuntimeException("Homestay không tồn tại"));

        // Check total number of images after upload
        int totalImages = (homestay.getImages() != null ? homestay.getImages().size() : 0) + images.length;
        if (totalImages > 5) {
            throw new IllegalArgumentException("Tổng số ảnh không được vượt quá 5");
        }

        List<String> newFilenames = new ArrayList<>();
        List<String> newUrls = new ArrayList<>();

        try {
            // Create upload directory if it doesn't exist
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            for (MultipartFile image : images) {
                // Validate image
                validateImage(image);

                // Generate unique filename with timestamp and random number
                String originalFilename = image.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String filename = System.currentTimeMillis() + "_" + Math.round(Math.random() * 1000) + extension;

                Path targetPath = rootLocation.resolve(filename);

                // Create directories if they don't exist
                targetPath.getParent().toFile().mkdirs();

                // Save file with atomic operation
                Path tempFile = Files.createTempFile(rootLocation, "temp-", extension);
                Files.copy(image.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
                Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE);

                newFilenames.add(filename);
                newUrls.add("/homestay_images/" + filename);
            }

            // Initialize lists if null
            if (homestay.getImages() == null) homestay.setImages(new ArrayList<>());
            if (homestay.getImageUrls() == null) homestay.setImageUrls(new ArrayList<>());

            // Update homestay with all new images
            homestay.getImages().addAll(newFilenames);
            homestay.getImageUrls().addAll(newUrls);

            homestayRepository.saveAndFlush(homestay);

        } catch (Exception e) {
            // Cleanup any uploaded files if there was an error
            for (String filename : newFilenames) {
                try {
                    Files.deleteIfExists(rootLocation.resolve(filename));
                } catch (IOException ignored) {
                    // Ignore cleanup errors
                }
            }
            throw new RuntimeException("Lỗi khi tải ảnh lên: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Homestay updateHomestayImages(Homestay homestay, List<String> currentImages, MultipartFile[] newImages) throws IOException {
        // Validate total number of images
        int totalImages = (currentImages != null ? currentImages.size() : 0) +
                (newImages != null ? newImages.length : 0);
        if (totalImages > 5) {
            throw new IllegalArgumentException("Tổng số ảnh không được vượt quá 5");
        }

        // Get current image filenames from paths
        List<String> currentFilenames = currentImages.stream()
                .map(path -> path.substring(path.lastIndexOf('/') + 1))
                .collect(Collectors.toList());

        // Delete removed images
        if (homestay.getImages() != null) {
            List<String> existingImages = new ArrayList<>(homestay.getImages());
            for (String existingImage : existingImages) {
                if (!currentFilenames.contains(existingImage)) {
                    deleteImage(existingImage);
                    homestay.getImages().remove(existingImage);
                    homestay.getImageUrls().remove("/homestay_images/" + existingImage);
                }
            }
        }

        // Upload new images if any
        if (newImages != null && newImages.length > 0) {
            List<String> newFilenames = new ArrayList<>();
            List<String> newUrls = new ArrayList<>();

            try {
                for (MultipartFile image : newImages) {
                    validateImage(image);
                    String originalFilename = image.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String filename = System.currentTimeMillis() + "_" + Math.round(Math.random() * 1000) + extension;

                    Path targetPath = rootLocation.resolve(filename);
                    Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                    newFilenames.add(filename);
                    newUrls.add("/homestay_images/" + filename);
                }

                homestay.getImages().addAll(newFilenames);
                homestay.getImageUrls().addAll(newUrls);

            } catch (Exception e) {
                // Cleanup any new files if there was an error
                for (String filename : newFilenames) {
                    try {
                        Files.deleteIfExists(rootLocation.resolve(filename));
                    } catch (IOException ignored) {
                        // Ignore cleanup errors
                    }
                }
                throw e;
            }
        }

        return homestayRepository.saveAndFlush(homestay);
    }

    private void deleteImage(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting image: " + filename, e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File rỗng");
        }

        // Check file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh");
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Chỉ chấp nhận file có định dạng: " + String.join(", ", allowedExtensions));
        }
    }

    private void validateHomestay(Homestay homestay) {
        List<String> errors = new ArrayList<>();

        if (homestay.getName() == null || homestay.getName().trim().isEmpty()) {
            errors.add("Tên homestay không được để trống");
        } else if (homestay.getName().length() < 5 || homestay.getName().length() > 100) {
            errors.add("Tên homestay phải từ 5 đến 100 ký tự");
        }

        if (homestay.getLocation() == null || homestay.getLocation().trim().isEmpty()) {
            errors.add("Địa điểm không được để trống");
        }

        if (homestay.getPrice() == null || homestay.getPrice() <= 0) {
            errors.add("Giá phải lớn hơn 0");
        }

        if (homestay.getCapacity() == null || homestay.getCapacity() < 1) {
            errors.add("Sức chứa phải ít nhất là 1");
        }

        if (homestay.getAmenities() != null && homestay.getAmenities().size() > 10) {
            errors.add("Tối đa 10 tiện nghi");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }

    /**
     * Kiểm tra xem người dùng đã từng đặt homestay này chưa
     *
     * @param username Tên người dùng
     * @param homestayId ID của homestay
     * @return true nếu người dùng đã từng đặt homestay này, false nếu chưa
     */
    public boolean hasUserBookedHomestay(String username, Long homestayId) {
        log.info("Checking if user {} has booked homestay {}", username, homestayId);
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            // Đếm số lượng booking đã hoàn thành của người dùng cho homestay này
            // Long bookingCount = bookingRepository.countByUserAndHomestayAndStatus(
            //         user.getId(), homestayId, "COMPLETED");
            Long bookingCount = 0L; // Temporary fix

            log.info("User {} has {} completed bookings for homestay {}",
                    username, bookingCount, homestayId);

            return bookingCount > 0;
        } catch (Exception e) {
            log.error("Error checking if user has booked homestay: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Upload ảnh đánh giá cho homestay
     *
     * @param homestayId ID của homestay
     * @param images Mảng ảnh cần upload
     * @return Danh sách đường dẫn các ảnh đã upload
     * @throws IOException Nếu có lỗi khi upload ảnh
     */
    public List<String> uploadReviewImages(Long homestayId, MultipartFile[] images) throws IOException {
        log.info("Uploading {} review images for homestay {}", images.length, homestayId);

        // Tạo thư mục cho ảnh đánh giá nếu chưa tồn tại
        String reviewImageDir = UPLOAD_DIR + "/reviews/" + homestayId;
        Path reviewDirPath = Paths.get(reviewImageDir);
        if (!Files.exists(reviewDirPath)) {
            Files.createDirectories(reviewDirPath);
        }

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }

            try {
                // Validate ảnh
                validateImage(image);

                // Tạo tên file duy nhất
                String originalFilename = image.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFilename = System.currentTimeMillis() + "_" + fileExtension;

                // Đường dẫn lưu file
                Path imagePath = reviewDirPath.resolve(newFilename);

                // Lưu file
                Files.copy(image.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

                // Tạo đường dẫn tương đối để lưu vào database
                String relativeImageUrl = "/homestay_images/reviews/" + homestayId + "/" + newFilename;
                imageUrls.add(relativeImageUrl);

                log.info("Uploaded review image: {}", relativeImageUrl);
            } catch (Exception e) {
                log.error("Error uploading review image: {}", e.getMessage(), e);
                throw new IOException("Không thể upload ảnh: " + e.getMessage(), e);
            }
        }

        return imageUrls;
    }
}