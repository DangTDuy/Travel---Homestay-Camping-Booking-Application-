package ut.edu.project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ut.edu.project.models.Travel;
import ut.edu.project.models.User;
import ut.edu.project.services.TravelService;
// import ut.edu.project.services.UserService; // Không còn cần thiết
// import ut.edu.project.dtos.UserDTO; // Không còn cần thiết
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
// Đã loại bỏ các import không cần thiết

@Controller
@RequestMapping("/admin/travel")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminTravelController {

    @Autowired
    private TravelService travelService;

    // Đã loại bỏ vì không còn cần thiết
    // @Autowired
    // private UserService userService;

    private static final Logger log = LoggerFactory.getLogger(AdminTravelController.class);

    @GetMapping
    public String adminTravelPage(Model model, @RequestParam(required = false) String location) {
        try {
            // Lấy danh sách tour du lịch, có thể lọc theo location nếu có
            List<Travel> travels = (location != null && !location.isEmpty())
                ? travelService.searchTravels(location, null, null, null, null, null, null)
                : travelService.getAllTravels();

            model.addAttribute("travels", travels);
            // Đã loại bỏ việc truyền danh sách độ khó

            // Đã loại bỏ việc lấy danh sách hướng dẫn viên vì không còn cần thiết

            // Thêm travel mới cho form
            model.addAttribute("travel", new Travel());
            model.addAttribute("currentPage", "admin/travel");

        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách tour: " + e.getMessage());
            model.addAttribute("currentPage", "admin/travel");
            log.error("Error loading travels", e);
        }
        return "admin/admin-travel";
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createTravel(
            @Valid @ModelAttribute Travel travel,
            BindingResult result,
            @RequestParam(name = "includedServices", required = false) List<String> includedServices,
            @RequestParam(name = "images", required = false) MultipartFile[] images) {

        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        try {
            // Xử lý included services
            if (includedServices != null && !includedServices.isEmpty()) {
                travel.setIncludedServices(includedServices);
            }

            // Lưu tour mới
            Travel savedTravel = travelService.createTravel(travel);

            // Upload ảnh nếu có
            if (images != null && images.length > 0) {
                // Đã sử dụng uploadImages đã được cải tiến để loại bỏ trùng lặp
                travelService.uploadImages(savedTravel.getId(), images);
                // Lấy lại travel sau khi upload ảnh
                savedTravel = travelService.getTravelById(savedTravel.getId())
                        .orElse(savedTravel);

                log.info("Travel created with {} images",
                        savedTravel.getImageUrls() != null ? savedTravel.getImageUrls().size() : 0);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo tour du lịch mới thành công",
                "travel", savedTravel
            ));
        } catch (Exception e) {
            log.error("Error creating travel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getTravelDetails(@PathVariable Long id) {
        try {
            Travel travel = travelService.getTravelById(id)
                .orElseThrow(() -> new RuntimeException("Tour du lịch không tồn tại"));

            // Kiểm tra danh sách URL ảnh
            if (travel.getImageUrls() != null) {
                log.info("Travel {} has {} unique images", id, travel.getImageUrls().size());
            }

            return ResponseEntity.ok(travel);
        } catch (Exception e) {
            log.error("Error getting travel details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateTravel(
            @PathVariable Long id,
            @Valid @ModelAttribute Travel travel,
            BindingResult result,
            @RequestParam(required = false) List<String> includedServices,
            @RequestParam(required = false) MultipartFile[] newImages,
            @RequestParam(required = false) List<String> existingImages,
            @RequestParam(required = false) List<String> removedImages) {

        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        try {
            // Xử lý included services
            if (includedServices != null) {
                travel.setIncludedServices(includedServices);
            } else {
                travel.setIncludedServices(new ArrayList<>());
            }

            // Log trước khi cập nhật
            log.info("Updating travel with ID: {}, isAvailable: {}", id, travel.isAvailable());

            // Cập nhật thông tin cơ bản của tour
            Travel updatedTravel = travelService.updateTravel(id, travel);

            // Log sau khi cập nhật
            log.info("Updated travel with ID: {}, isAvailable: {}", id, updatedTravel.isAvailable());

            // Log thông tin về ảnh
            log.info("Existing images count: {}", existingImages != null ? existingImages.size() : 0);
            if (existingImages != null && !existingImages.isEmpty()) {
                log.info("Existing images: {}", existingImages);
            }

            // Log thông tin về ảnh đã xóa
            if (removedImages != null && !removedImages.isEmpty()) {
                log.info("Removed images count: {}", removedImages.size());
                log.info("Removed images: {}", removedImages);
            }

            log.info("New images count: {}", newImages != null ? newImages.length : 0);

            // Cập nhật ảnh
            updatedTravel = travelService.updateTravelImages(updatedTravel, existingImages, newImages);

            // Log sau khi cập nhật ảnh
            log.info("Updated travel images. Total images: {}", updatedTravel.getImageUrls().size());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật tour du lịch thành công",
                "travel", updatedTravel
            ));
        } catch (Exception e) {
            log.error("Error updating travel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<?> deleteTravel(@PathVariable Long id) {
        try {
            travelService.deleteTravel(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting travel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // API endpoint để lấy danh sách tour theo AJAX
    @GetMapping("/api/list")
    public ResponseEntity<List<Travel>> getTravelList(
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "priceRange", required = false) String priceRange,
            @RequestParam(name = "duration", required = false) String duration,
            @RequestParam(name = "difficulty", required = false) String difficulty) {

        Double minPrice = null;
        Double maxPrice = null;
        Integer minDays = null;
        Integer maxDays = null;
        Travel.DifficultyLevel difficultyLevel = null;

        // Parse priceRange
        if (priceRange != null && !priceRange.isEmpty()) {
            String[] parts = priceRange.split("-");
            if (parts.length == 2) {
                try {
                    if (!parts[0].isEmpty()) {
                        minPrice = Double.parseDouble(parts[0]);
                    }
                    if (!parts[1].isEmpty()) {
                        maxPrice = Double.parseDouble(parts[1]);
                    }
                } catch (NumberFormatException e) {
                    log.error("Error parsing price range", e);
                }
            }
        }

        // Parse duration
        if (duration != null && !duration.isEmpty()) {
            String[] parts = duration.split("-");
            if (parts.length == 2) {
                try {
                    if (!parts[0].isEmpty()) {
                        minDays = Integer.parseInt(parts[0]);
                    }
                    if (!parts[1].isEmpty()) {
                        maxDays = Integer.parseInt(parts[1]);
                    }
                } catch (NumberFormatException e) {
                    log.error("Error parsing duration", e);
                }
            }
        }

        // Parse difficulty
        if (difficulty != null && !difficulty.isEmpty()) {
            try {
                difficultyLevel = Travel.DifficultyLevel.valueOf(difficulty);
            } catch (IllegalArgumentException e) {
                log.error("Error parsing difficulty level", e);
            }
        }

        // Tìm kiếm tour
        List<Travel> travels = travelService.searchTravels(
                location, minPrice, maxPrice, minDays, maxDays, difficultyLevel, null);

        return ResponseEntity.ok(travels);
    }
}