package ut.edu.project.controllers;

import jakarta.transaction.Transactional;
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
import ut.edu.project.models.Camping;
import ut.edu.project.services.CampingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.edu.project.services.UserService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCampingController {

    @Autowired
    private CampingService campingService;

    @Autowired
    private UserService userService;

    private static final Logger log = LoggerFactory.getLogger(AdminCampingController.class);

    @GetMapping("/camping")
    public String showAdminCampingPage(Model model) {
        model.addAttribute("currentPage", "admin/camping");
        return "admin/admin-camping";
    }

    @GetMapping("/api/campings")
    @ResponseBody
    public List<Camping> getAllCampings() {
        return campingService.getAllCampings();
    }

    @PostMapping("/api/campings")
    @ResponseBody
    public Camping addCamping(@RequestBody Camping camping) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return campingService.createCamping(camping, username);
    }

    @PutMapping("/api/campings/{id}")
    @ResponseBody
    public Camping updateCamping(@PathVariable Long id, @RequestBody Camping camping) {
        return campingService.updateCamping(id, camping);
    }

    @DeleteMapping("/api/campings/{id}")
    @ResponseBody
    public void deleteCamping(@PathVariable Long id) {
        campingService.deleteCamping(id);
    }

    @GetMapping
    public String adminCampingPage(Model model,
            @RequestParam(name = "location", required = false) String location,
            Authentication authentication) {
        try {
            log.info("Admin camping page requested by user: {}", authentication != null ? authentication.getName() : "anonymous");
            log.info("User roles: {}", authentication != null ? authentication.getAuthorities() : "none");

            List<Camping> campings;
            if (location != null && !location.isEmpty()) {
                log.info("Searching campings with location: {}", location);
                campings = campingService.searchCampings(location, null, null);
            } else {
                log.info("Getting all campings");
                campings = campingService.getAllCampings();
            }
            log.info("Found {} campings", campings.size());
            model.addAttribute("campings", campings);
            model.addAttribute("currentPage", "admin/camping");
        } catch (Exception e) {
            log.error("Error loading campings: {}", e.getMessage(), e);
            model.addAttribute("error", "Không thể tải danh sách khu cắm trại: " + e.getMessage());
            model.addAttribute("currentPage", "admin/camping");
        }
        return "admin/admin-camping";
    }

    @PostMapping("/create")
    public String createCamping(
            @Valid @ModelAttribute Camping camping,
            BindingResult result,
            Authentication authentication,
            Model model) {
        if (result.hasErrors()) {
            List<Camping> campings = campingService.searchCampings(null, null, null);
            model.addAttribute("campings", campings);
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin nhập vào");
            return "admin/admin-camping";
        }
        try {
            String username = authentication.getName();
            campingService.createCamping(camping, username);
            return "redirect:/admin/camping";
        } catch (RuntimeException e) {
            List<Camping> campings = campingService.searchCampings(null, null, null);
            model.addAttribute("campings", campings);
            model.addAttribute("error", e.getMessage());
            return "admin/admin-camping";
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCamping(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Attempting to delete camping with id: {}", id);
            campingService.deleteCamping(id);
            log.info("Successfully deleted camping with id: {}", id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa khu cắm trại thành công");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error deleting camping {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            log.error("Unexpected error deleting camping {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi xóa khu cắm trại: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createCampingWithImages(
            @RequestParam("name") String name,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("capacity") Integer capacity,
            @RequestParam("availableSlots") Integer availableSlots,
            @RequestParam("facilities") String facilities,
            @RequestParam("equipment") String equipment,
            @RequestParam("rules") String rules,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            Authentication authentication) {
        try {
            // Validate input
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Tên khu cắm trại không được để trống");
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
            if (availableSlots == null || availableSlots < 0) {
                throw new IllegalArgumentException("Số chỗ trống không được âm");
            }
            if (facilities == null || facilities.trim().isEmpty()) {
                throw new IllegalArgumentException("Tiện nghi không được để trống");
            }

            Camping camping = new Camping();
            camping.setName(name.trim());
            camping.setLocation(location.trim());
            camping.setDescription(description.trim());
            camping.setPrice(price);
            camping.setCapacity(capacity);
            camping.setAvailableSlots(availableSlots);
            camping.setFacilities(new ArrayList<>(List.of(facilities.split(","))));
            camping.setEquipment(new ArrayList<>(List.of(equipment.split(","))));
            camping.setRules(new ArrayList<>(List.of(rules.split(","))));
            camping.setImages(new ArrayList<>());
            camping.setImageUrls(new ArrayList<>());

            String username = authentication.getName();
            Camping savedCamping = campingService.createCamping(camping, username);

            if (images != null && images.length > 0) {
                campingService.uploadImages(savedCamping.getId(), images);
                // Lấy lại camping sau khi upload ảnh
                savedCamping = campingService.getCampingById(savedCamping.getId())
                        .orElse(savedCamping);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm khu cắm trại thành công!");
            response.put("camping", savedCamping);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid argument during camping creation: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error creating camping: {}", e.getMessage(), e);
            log.error("Stack trace:", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + (e.getMessage() != null ? e.getMessage() : "Vui lòng kiểm tra log server"));
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/update")
    @ResponseBody
    public ResponseEntity<?> updateCampingWithImages(
            @RequestParam("campingId") Long campingId,
            @RequestParam("name") String name,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("capacity") Integer capacity,
            @RequestParam("availableSlots") Integer availableSlots,
            @RequestParam("facilities") String facilities,
            @RequestParam("equipment") String equipment,
            @RequestParam("rules") String rules,
            @RequestParam("currentImages") String currentImagesJson,
            @RequestParam(value = "newImages", required = false) MultipartFile[] newImages,
            Authentication authentication) {
        try {
            // Validate input
            if (campingId == null) {
                throw new IllegalArgumentException("ID khu cắm trại không hợp lệ");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Tên khu cắm trại không được để trống");
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
            if (availableSlots == null || availableSlots < 0) {
                throw new IllegalArgumentException("Số chỗ trống không được âm");
            }
            if (facilities == null || facilities.trim().isEmpty()) {
                throw new IllegalArgumentException("Tiện nghi không được để trống");
            }

            // Parse current images JSON
            ObjectMapper mapper = new ObjectMapper();
            List<String> currentImages = mapper.readValue(currentImagesJson,
                    new TypeReference<List<String>>() {});

            // Update camping
            Camping camping = campingService.getCampingById(campingId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khu cắm trại"));

            camping.setName(name.trim());
            camping.setLocation(location.trim());
            camping.setDescription(description.trim());
            camping.setPrice(price);
            camping.setCapacity(capacity);
            camping.setAvailableSlots(availableSlots);
            camping.setFacilities(new ArrayList<>(List.of(facilities.split(","))));
            camping.setEquipment(new ArrayList<>(List.of(equipment.split(","))));
            camping.setRules(new ArrayList<>(List.of(rules.split(","))));

            // Update images
            camping = campingService.updateCampingImages(camping, currentImages, newImages);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật khu cắm trại thành công!");
            response.put("camping", camping);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid argument during camping update: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error updating camping: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi cập nhật khu cắm trại: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<?> getAllCampings(
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "priceRange", required = false) String priceRange) {
        try {
            List<Camping> campings;
            
            if ((location != null && !location.isEmpty()) || (priceRange != null && !priceRange.isEmpty())) {
                Double minPrice = null;
                Double maxPrice = null;
                
                if (priceRange != null && !priceRange.isEmpty()) {
                    String[] prices = priceRange.split("-");
                    if (prices.length == 2) {
                        try {
                            minPrice = Double.parseDouble(prices[0]);
                            maxPrice = Double.parseDouble(prices[1]);
                        } catch (NumberFormatException e) {
                            log.error("Invalid price range format: {}", priceRange);
                        }
                    }
                }
                
                campings = campingService.searchCampings(location, minPrice, maxPrice);
            } else {
                campings = campingService.getAllCampings();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("campings", campings);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting campings: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy danh sách khu cắm trại: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/api/get/{id}")
    @ResponseBody
    public ResponseEntity<?> getCampingById(@PathVariable Long id) {
        try {
            Camping camping = campingService.getCampingById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khu cắm trại"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("camping", camping);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Camping not found: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error getting camping: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy thông tin khu cắm trại: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/images")
    @ResponseBody
    public ResponseEntity<?> uploadImages(@PathVariable Long id,
                                          @RequestParam("images") MultipartFile[] images,
                                          Authentication authentication) {
        try {
            campingService.uploadImages(id, images);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tải lên hình ảnh thành công");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error uploading images: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error uploading images: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi tải lên hình ảnh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/delete-images")
    @Transactional
    public ResponseEntity<?> deleteCampingImages(
            @RequestParam("campingId") Long campingId,
            @RequestParam("imageUrls") List<String> imageUrls) {
        try {
            campingService.deleteImages(campingId, imageUrls);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa hình ảnh thành công");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error deleting images: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error deleting images: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi xóa hình ảnh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 