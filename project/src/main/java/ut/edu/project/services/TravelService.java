package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ut.edu.project.models.Travel;
import ut.edu.project.repositories.TravelRepository;
import ut.edu.project.models.Travel.DifficultyLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TravelService {

    private static final Logger log = LoggerFactory.getLogger(TravelService.class);
    private final Path rootLocation = Paths.get("src/main/resources/static/travel_images");

    @Autowired
    private TravelRepository travelRepository;

    @Transactional
    public Travel createTravel(Travel travel) {
        validateTravel(travel);
        return travelRepository.save(travel);
    }

    @Transactional
    public Travel updateTravel(Long id, Travel travel) {
        Travel existingTravel = travelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));

        // Cập nhật thông tin
        existingTravel.setTourName(travel.getTourName());
        existingTravel.setLocation(travel.getLocation());
        existingTravel.setDescription(travel.getDescription());
        existingTravel.setPrice(travel.getPrice());
        existingTravel.setItinerary(travel.getItinerary());
        // Đã loại bỏ trường guide
        existingTravel.setMaxParticipants(travel.getMaxParticipants());
        existingTravel.setMinParticipants(travel.getMinParticipants());
        existingTravel.setDurationDays(travel.getDurationDays());
        existingTravel.setImageUrls(travel.getImageUrls());
        existingTravel.setIncludedServices(travel.getIncludedServices());
        existingTravel.setRequirements(travel.getRequirements());
        existingTravel.setHighlights(travel.getHighlights());
        existingTravel.setDifficultyLevel(travel.getDifficultyLevel());
        existingTravel.setAvailable(travel.isAvailable());

        validateTravel(existingTravel);
        return travelRepository.save(existingTravel);
    }

    public Optional<Travel> getTravelById(Long id) {
        return travelRepository.findById(id);
    }

    public List<Travel> getAllTravels() {
        return travelRepository.findAll();
    }

    public List<Travel> searchTravels(
            String location,
            Double minPrice,
            Double maxPrice,
            Integer minDays,
            Integer maxDays,
            DifficultyLevel difficultyLevel,
            Boolean isAvailable) {
        return travelRepository.searchTravels(
                location, minPrice, maxPrice, minDays, maxDays, difficultyLevel, isAvailable);
    }

    // Đã loại bỏ phương thức getTravelsByGuide

    public List<Travel> getAvailableTravels() {
        return travelRepository.findByIsAvailableTrue();
    }

    public List<Travel> getTravelsByDifficultyLevel(DifficultyLevel level) {
        return travelRepository.findByDifficultyLevel(level);
    }

    public List<Travel> getTravelsByDuration(Integer minDays, Integer maxDays) {
        return travelRepository.findByDurationDaysBetween(minDays, maxDays);
    }

    public List<Travel> getTravelsByIncludedServices(List<String> services) {
        return travelRepository.findByIncludedServicesIn(services);
    }

    public List<Travel> getConfirmedTours() {
        return travelRepository.findConfirmedTours();
    }

    public List<Travel> getAvailableTours() {
        return travelRepository.findAvailableTours();
    }

    public List<Travel> getTopRatedTravels() {
        return travelRepository.findTop10ByOrderByRatingDesc();
    }

    public List<Travel> getMostBookedTravels() {
        return travelRepository.findTop10ByOrderByBookingCountDesc();
    }

    public List<Travel> getRecentTravels() {
        return travelRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional
    public void deleteTravel(Long id) {
        Travel travel = travelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));

        if (!travel.getBookings().isEmpty()) {
            throw new RuntimeException("Không thể xóa tour đã có đặt chỗ");
        }

        travelRepository.delete(travel);
    }

    private void validateTravel(Travel travel) {
        if (travel.getTourName() == null || travel.getTourName().trim().isEmpty()) {
            throw new RuntimeException("Tên tour không được để trống");
        }
        if (travel.getLocation() == null || travel.getLocation().trim().isEmpty()) {
            throw new RuntimeException("Địa điểm không được để trống");
        }
        if (travel.getPrice() == null || travel.getPrice() <= 0) {
            throw new RuntimeException("Giá phải lớn hơn 0");
        }
        if (travel.getItinerary() == null || travel.getItinerary().trim().isEmpty()) {
            throw new RuntimeException("Lịch trình không được để trống");
        }
        // Không kiểm tra guide nữa vì đã loại bỏ yêu cầu này
        if (travel.getMaxParticipants() == null || travel.getMaxParticipants() <= 0) {
            throw new RuntimeException("Số người tham gia tối đa phải lớn hơn 0");
        }
        if (travel.getMinParticipants() == null || travel.getMinParticipants() <= 0) {
            throw new RuntimeException("Số người tham gia tối thiểu phải lớn hơn 0");
        }
        if (travel.getMinParticipants() > travel.getMaxParticipants()) {
            throw new RuntimeException("Số người tham gia tối thiểu phải nhỏ hơn hoặc bằng số người tối đa");
        }
        if (travel.getDurationDays() == null || travel.getDurationDays() <= 0) {
            throw new RuntimeException("Số ngày tour phải lớn hơn 0");
        }
    }

    public long getTotalTravelCount() {
        return travelRepository.count();
    }

    public long getAvailableTravelCount() {
        return travelRepository.countByIsAvailableTrue();
    }

    public Travel saveTravel(Travel travel) {
        return travelRepository.save(travel);
    }

    /**
     * Upload ảnh cho travel
     * @param travelId ID của travel
     * @param images Mảng ảnh cần upload
     * @throws IOException Nếu có lỗi khi upload ảnh
     */
    @Transactional
    public synchronized void uploadImages(Long travelId, MultipartFile[] images) throws IOException {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 ảnh");
        }

        Travel travel = travelRepository.findById(travelId)
            .orElseThrow(() -> new RuntimeException("Travel không tồn tại"));

        // Tạo thư mục nếu chưa tồn tại
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }

        List<String> newUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }

            String originalFilename = image.getOriginalFilename();
            if (originalFilename == null) {
                continue;
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = System.currentTimeMillis() + "_" + Math.round(Math.random() * 1000) + extension;

            Path targetPath = rootLocation.resolve(filename);
            Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            newUrls.add("/travel_images/" + filename);
        }

        // Đặt danh sách ảnh mới thay vì thêm vào danh sách hiện có
        travel.setImageUrls(newUrls);
        travelRepository.save(travel);
    }

    /**
     * Cập nhật ảnh cho travel
     * @param travel Đối tượng travel cần cập nhật
     * @param currentImages Danh sách ảnh hiện tại
     * @param newImages Mảng ảnh mới
     * @return Travel đã được cập nhật
     * @throws IOException Nếu có lỗi khi xử lý ảnh
     */
    @Transactional
    public Travel updateTravelImages(Travel travel, List<String> currentImages, MultipartFile[] newImages) throws IOException {
        // Kiểm tra tổng số ảnh
        int totalImages = (currentImages != null ? currentImages.size() : 0) +
                (newImages != null ? newImages.length : 0);
        if (totalImages > 5) {
            throw new IllegalArgumentException("Tổng số ảnh không được vượt quá 5");
        }

        if (totalImages == 0) {
            throw new IllegalArgumentException("Phải có ít nhất 1 ảnh cho tour du lịch");
        }

        // Log trước khi cập nhật ảnh
        log.info("Updating travel images. Current images count: {}, New images count: {}",
                 currentImages != null ? currentImages.size() : 0,
                 newImages != null ? newImages.length : 0);

        // Lấy danh sách ảnh hiện tại trước khi cập nhật
        List<String> oldImageUrls = new ArrayList<>();
        if (travel.getImageUrls() != null) {
            oldImageUrls.addAll(travel.getImageUrls());
        }
        log.info("Old image URLs: {}", oldImageUrls);

        // Xóa danh sách ảnh hiện tại và tạo danh sách mới
        travel.setImageUrls(new ArrayList<>());

        // Thêm các ảnh hiện tại được chọn vào danh sách mới
        if (currentImages != null && !currentImages.isEmpty()) {
            // Sử dụng Set để loại bỏ trùng lặp
            Set<String> uniqueImages = new HashSet<>(currentImages);
            travel.getImageUrls().addAll(uniqueImages);
            log.info("Added {} existing images to travel", uniqueImages.size());
        }

        // Xóa các file ảnh không còn được sử dụng
        if (currentImages != null) {
            for (String oldUrl : oldImageUrls) {
                if (!currentImages.contains(oldUrl)) {
                    // Xóa file ảnh
                    String filename = oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
                    deleteImage(filename);
                    log.info("Deleted unused image: {}", filename);
                }
            }
        } else {
            // Nếu không có ảnh hiện tại nào được gửi lên, xóa tất cả ảnh cũ
            for (String oldUrl : oldImageUrls) {
                String filename = oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
                deleteImage(filename);
                log.info("Deleted all old images: {}", filename);
            }
        }

        // Thêm ảnh mới nếu có
        if (newImages != null && newImages.length > 0) {
            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }

            for (MultipartFile image : newImages) {
                if (image.isEmpty()) {
                    continue;
                }

                String originalFilename = image.getOriginalFilename();
                if (originalFilename == null) {
                    continue;
                }
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String filename = System.currentTimeMillis() + "_" + Math.round(Math.random() * 1000) + extension;

                Path targetPath = rootLocation.resolve(filename);
                Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                String imageUrl = "/travel_images/" + filename;
                travel.getImageUrls().add(imageUrl);
                log.info("Added new image: {}", imageUrl);
            }
            log.info("Added {} new images to travel", travel.getImageUrls().size() - (currentImages != null ? currentImages.size() : 0));
        }

        return travelRepository.save(travel);
    }

    /**
     * Xóa ảnh
     * @param filename Tên file cần xóa
     */
    private void deleteImage(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
                log.info("Successfully deleted file: {}", filename);
            } else {
                log.warn("File not found for deletion: {}", filename);
            }
        } catch (IOException e) {
            log.error("Error deleting image: {}", filename, e);
            // Không throw exception để tránh làm gian đoạn quá trình cập nhật
        }
    }
}
