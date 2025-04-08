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
import ut.edu.project.models.Homestay;
import ut.edu.project.services.HomestayService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin/homestay")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminHomestayController {

    @Autowired
    private HomestayService homestayService;

    private static final Logger log = LoggerFactory.getLogger(AdminHomestayController.class);

    @GetMapping
    public String adminHomestayPage(Model model, @RequestParam(required = false) String location, Authentication authentication) {
        try {
            log.info("Admin homestay page requested by user: {}", authentication != null ? authentication.getName() : "anonymous");
            log.info("User roles: {}", authentication != null ? authentication.getAuthorities() : "none");
            
            List<Homestay> homestays;
            if (location != null && !location.isEmpty()) {
                log.info("Searching homestays with location: {}", location);
                homestays = homestayService.searchHomestays(location, null, null);
            } else {
                log.info("Getting all homestays");
                homestays = homestayService.getAllHomestays();
            }
            log.info("Found {} homestays", homestays.size());
            model.addAttribute("homestays", homestays);
        } catch (Exception e) {
            log.error("Error loading homestays: {}", e.getMessage(), e);
            model.addAttribute("error", "Không thể tải danh sách homestay: " + e.getMessage());
        }
        return "admin/admin-homestay";
    }

    @PostMapping("/create")
    public String createHomestay(
            @Valid @ModelAttribute Homestay homestay,
            BindingResult result,
            Authentication authentication,
            Model model) {
        if (result.hasErrors()) {
            List<Homestay> homestays = homestayService.searchHomestays(null, null, null);
            model.addAttribute("homestays", homestays);
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin nhập vào");
            return "admin/admin-homestay";
        }
        try {
            String username = authentication.getName();
            homestayService.createHomestay(homestay, username);
            return "redirect:/admin/homestay";
        } catch (RuntimeException e) {
            List<Homestay> homestays = homestayService.searchHomestays(null, null, null);
            model.addAttribute("homestays", homestays);
            model.addAttribute("error", e.getMessage());
            return "admin/admin-homestay";
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteHomestay(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Attempting to delete homestay with id: {}", id);
            homestayService.deleteHomestay(id);
            log.info("Successfully deleted homestay with id: {}", id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa homestay thành công");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error deleting homestay {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            log.error("Unexpected error deleting homestay {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi xóa homestay: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createHomestayWithImages(
            @RequestParam("name") String name,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("capacity") Integer capacity,
            @RequestParam("amenities") String amenities,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            Authentication authentication) {
        try {
            // Validate input
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Tên homestay không được để trống");
            }
            if (location == null || location.trim().isEmpty()) {
                throw new IllegalArgumentException("Địa điểm không được để trống");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Mô tả không được để trống");
            }
            if (price == null || price <= 0) {
                throw new IllegalArgumentException("Giá phải lớn hơn 0");
            }
            if (capacity == null || capacity <= 0) {
                throw new IllegalArgumentException("Sức chứa phải lớn hơn 0");
            }
            if (amenities == null || amenities.trim().isEmpty()) {
                throw new IllegalArgumentException("Tiện nghi không được để trống");
            }

            Homestay homestay = new Homestay();
            homestay.setName(name.trim());
            homestay.setLocation(location.trim());
            homestay.setDescription(description.trim());
            homestay.setPrice(price);
            homestay.setCapacity(capacity);
            homestay.setAmenities(new ArrayList<>(List.of(amenities.split(","))));
            homestay.setImages(new ArrayList<>());
            homestay.setImageUrls(new ArrayList<>());

            String username = authentication.getName();
            Homestay savedHomestay = homestayService.createHomestay(homestay, username);

            if (images != null && images.length > 0) {
                homestayService.uploadImages(savedHomestay.getId(), images);
                // Lấy lại homestay sau khi upload ảnh
                savedHomestay = homestayService.getHomestayById(savedHomestay.getId())
                        .orElse(savedHomestay);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm homestay thành công!");
            response.put("homestay", savedHomestay);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid argument during homestay creation: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error creating homestay: {}", e.getMessage(), e);
            log.error("Stack trace:", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + (e.getMessage() != null ? e.getMessage() : "Vui lòng kiểm tra log server"));
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/update")
    @ResponseBody
    public ResponseEntity<?> updateHomestayWithImages(
            @RequestParam("homestayId") Long homestayId,
            @RequestParam("name") String name,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("capacity") Integer capacity,
            @RequestParam("amenities") String amenities,
            @RequestParam("currentImages") String currentImagesJson,
            @RequestParam(value = "newImages", required = false) MultipartFile[] newImages,
            Authentication authentication) {
        try {
            // Validate input
            if (homestayId == null) {
                throw new IllegalArgumentException("ID homestay không hợp lệ");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Tên homestay không được để trống");
            }
            if (location == null || location.trim().isEmpty()) {
                throw new IllegalArgumentException("Địa điểm không được để trống");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Mô tả không được để trống");
            }
            if (price == null || price <= 0) {
                throw new IllegalArgumentException("Giá phải lớn hơn 0");
            }
            if (capacity == null || capacity <= 0) {
                throw new IllegalArgumentException("Sức chứa phải lớn hơn 0");
            }
            if (amenities == null || amenities.trim().isEmpty()) {
                throw new IllegalArgumentException("Tiện nghi không được để trống");
            }

            // Parse current images JSON
            ObjectMapper mapper = new ObjectMapper();
            List<String> currentImages = mapper.readValue(currentImagesJson, 
                new TypeReference<List<String>>() {});

            // Update homestay
            Homestay homestay = homestayService.getHomestayById(homestayId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy homestay"));

            homestay.setName(name.trim());
            homestay.setLocation(location.trim());
            homestay.setDescription(description.trim());
            homestay.setPrice(price);
            homestay.setCapacity(capacity);
            homestay.setAmenities(new ArrayList<>(List.of(amenities.split(","))));

            // Update images
            homestay = homestayService.updateHomestayImages(homestay, currentImages, newImages);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật homestay thành công!");
            response.put("homestay", homestay);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid argument during homestay update for id {}: {}", homestayId, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error updating homestay {}: {}", homestayId, e.getMessage(), e);
            log.error("Stack trace:", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + (e.getMessage() != null ? e.getMessage() : "Vui lòng kiểm tra log server"));
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<?> getAllHomestays(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String priceRange) {
        try {
            log.info("Getting homestays for admin with filters - location: {}, priceRange: {}", 
                    location, priceRange);
            
            List<Homestay> homestays;
            
            // Xử lý khoảng giá đặc biệt cho "trên 2 triệu"
            if (priceRange != null && priceRange.equals("2000000-")) {
                log.info("Handling special case for price over 2,000,000");
                String modifiedPriceRange = "2000000-999999999"; // Thêm giá trị tối đa giả
                homestays = homestayService.searchHomestays(location, modifiedPriceRange, null);
            } 
            // Xử lý các trường hợp tìm kiếm khác
            else if ((location != null && !location.isEmpty()) || (priceRange != null && !priceRange.isEmpty())) {
                homestays = homestayService.searchHomestays(location, priceRange, null);
                log.info("Found {} homestays matching search criteria", homestays.size());
            } else {
                // Không có tham số tìm kiếm
                homestays = homestayService.getAllHomestays();
                log.info("Found {} homestays (all)", homestays.size());
            }
            
            log.info("Preparing response for {} homestays.", homestays.size());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", homestays);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting homestays: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/get/{id}")
    @ResponseBody
    public ResponseEntity<?> getHomestayById(@PathVariable Long id) {
        try {
            log.info("API request to get homestay by id: {}", id);
            Homestay homestay = homestayService.getHomestayById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy homestay với ID: " + id));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", homestay);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Homestay not found for id {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error getting homestay by id {} for API: {}", id, e.getMessage(), e);
            log.error("Stack trace:", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thông tin homestay: " + (e.getMessage() != null ? e.getMessage() : "Vui lòng kiểm tra log server"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/images")
    @ResponseBody
    public ResponseEntity<?> uploadImages(@PathVariable Long id, 
                                        @RequestParam("images") MultipartFile[] images,
                                        Authentication authentication) {
        try {
            log.info("Uploading images for homestay {}", id);
            homestayService.uploadImages(id, images);
            
            // Get updated homestay with new images
            Homestay homestay = homestayService.getHomestayById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy homestay"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tải ảnh lên thành công");
            response.put("homestay", homestay);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error uploading images for homestay {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Unexpected error uploading images for homestay {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi tải ảnh lên: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}