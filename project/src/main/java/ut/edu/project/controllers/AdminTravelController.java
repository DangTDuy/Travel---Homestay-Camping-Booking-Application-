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
// import java.util.stream.Collectors; // Không còn cần thiết

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
            model.addAttribute("difficultyLevels", Travel.DifficultyLevel.values());

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

    @PostMapping("/create")
    public String createTravel(
            @Valid @ModelAttribute Travel travel,
            BindingResult result,
            @RequestParam(name = "includedServices", required = false) List<String> includedServices,
            @RequestParam(name = "images", required = false) List<MultipartFile> images,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("travels", travelService.getAllTravels());
            model.addAttribute("difficultyLevels", Travel.DifficultyLevel.values());
            // Đã loại bỏ việc thêm danh sách guides vào model
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin nhập vào");
            return "admin/admin-travel";
        }

        try {
            // Xử lý included services
            if (includedServices != null && !includedServices.isEmpty()) {
                travel.setIncludedServices(includedServices);
            }

            // Xử lý image uploads
            List<String> imageUrls = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        // Lưu ảnh và lấy URL - thông thường sẽ có service riêng để xử lý việc này
                        // Ở đây giả định URL được lưu trữ dưới dạng chuỗi
                        String imageUrl = "https://example.com/image/" + image.getOriginalFilename(); // Điều này cần được thay bằng code thực tế để lưu ảnh
                        imageUrls.add(imageUrl);
                    }
                }
                travel.setImageUrls(imageUrls);
            }

            // Lưu tour mới
            travelService.createTravel(travel);
            return "redirect:/admin/travel";
        } catch (Exception e) {
            model.addAttribute("travels", travelService.getAllTravels());
            model.addAttribute("difficultyLevels", Travel.DifficultyLevel.values());
            // Đã loại bỏ việc thêm danh sách guides vào model
            model.addAttribute("error", "Lỗi khi tạo tour du lịch: " + e.getMessage());
            log.error("Error creating travel", e);
            return "admin/admin-travel";
        }
    }

    @GetMapping("/{id}")
    public String getTravelDetails(@PathVariable Long id, Model model) {
        try {
            Travel travel = travelService.getTravelById(id)
                .orElseThrow(() -> new RuntimeException("Tour du lịch không tồn tại"));

            model.addAttribute("travel", travel);
            model.addAttribute("difficultyLevels", Travel.DifficultyLevel.values());
            // Đã loại bỏ việc thêm danh sách guides vào model

            return "admin/admin-travel-form";
        } catch (Exception e) {
            return "redirect:/admin/travel?error=" + e.getMessage();
        }
    }

    @PostMapping("/{id}")
    public String updateTravel(
            @PathVariable Long id,
            @Valid @ModelAttribute Travel travel,
            BindingResult result,
            @RequestParam(required = false) List<String> includedServices,
            @RequestParam(required = false) List<MultipartFile> newImages,
            @RequestParam(required = false) List<String> existingImages,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("difficultyLevels", Travel.DifficultyLevel.values());
            // Đã loại bỏ việc thêm danh sách guides vào model
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin nhập vào");
            return "admin/admin-travel-form";
        }

        try {
            // Xử lý included services
            if (includedServices != null) {
                travel.setIncludedServices(includedServices);
            } else {
                travel.setIncludedServices(new ArrayList<>());
            }

            // Xử lý image URLs
            List<String> imageUrls = new ArrayList<>();

            // Giữ lại các ảnh hiện có nếu được chọn
            if (existingImages != null && !existingImages.isEmpty()) {
                imageUrls.addAll(existingImages);
            }

            // Thêm ảnh mới nếu có
            if (newImages != null && !newImages.isEmpty()) {
                for (MultipartFile image : newImages) {
                    if (!image.isEmpty()) {
                        // Xử lý tương tự như trong phương thức create
                        String imageUrl = "https://example.com/image/" + image.getOriginalFilename();
                        imageUrls.add(imageUrl);
                    }
                }
            }

            travel.setImageUrls(imageUrls);

            // Cập nhật tour
            travelService.updateTravel(id, travel);
            return "redirect:/admin/travel";
        } catch (Exception e) {
            model.addAttribute("difficultyLevels", Travel.DifficultyLevel.values());
            // Đã loại bỏ việc thêm danh sách guides vào model
            model.addAttribute("error", "Lỗi khi cập nhật tour du lịch: " + e.getMessage());
            model.addAttribute("currentPage", "admin/travel");
            log.error("Error updating travel", e);
            return "admin/admin-travel-form";
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