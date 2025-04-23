package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ut.edu.project.models.Camping;
import ut.edu.project.services.CampingService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;

@Controller
@RequestMapping("/camping")
public class CampingController {
    @Autowired
    private CampingService campingService; // Dịch vụ quản lý khu cắm trại

    // Hiển thị danh sách khu cắm trại với tìm kiếm và phân trang
    @GetMapping
    public String showCampingList(
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            @RequestParam(name = "minPlaces", required = false) Integer minPlaces,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            Model model) {
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("minPlaces", minPlaces);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);

        Page<Camping> campingPage;
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            campingPage = campingService.searchCampingsByName(searchTerm, page, size, sort);
        } else if (minPlaces != null) {
            campingPage = campingService.getCampingsByMinPlaces(minPlaces, page, size, sort);
        } else {
            campingPage = campingService.getCampingsPaginated(page, size, sort, start, end);
        }

        model.addAttribute("campings", campingPage.getContent());
        model.addAttribute("totalPages", campingPage.getTotalPages());
        return "camping";
    }

    // Hiển thị trang chi tiết khu cắm trại theo ID
    @GetMapping("/{id}")
    public String getCampingDetailById(@PathVariable("id") Long id, Model model, @Autowired(required = false) CsrfToken csrfToken, Authentication authentication) {
        try {
            Camping camping = campingService.getCampingById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại"));
            
            model.addAttribute("camping", camping);
            model.addAttribute("currentPage", "camping");
            
            // Thêm thông tin đã đăng nhập và có thể đánh giá
            boolean hasBookedCamping = false;
            boolean canReview = false;
            boolean isOwner = false;
            
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                // Kiểm tra có phải chủ sở hữu không
                if (camping.getOwner() != null && camping.getOwner().getUsername() != null && 
                    camping.getOwner().getUsername().equals(username)) {
                    isOwner = true;
                }
                
                // Logic kiểm tra đã đặt camping và có thể đánh giá
                // Sẽ được bổ sung khi có đánh giá
            }
            
            model.addAttribute("hasBookedCamping", hasBookedCamping);
            model.addAttribute("canReview", canReview);
            model.addAttribute("isOwner", isOwner);
            
            return "camping/camping-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải chi tiết khu cắm trại: " + e.getMessage());
            return "camping/camping";
        }
    }

    // Lấy tất cả khu cắm trại qua API
    @GetMapping("/api")
    @ResponseBody
    public List<Camping> getAllCampings() {
        return campingService.getAllCampings();
    }

    // Lấy thông tin khu cắm trại theo ID qua API
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Camping> getCampingById(@PathVariable("id") Long id) {
        Optional<Camping> camping = campingService.getCampingById(id);
        return camping.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Thêm khu cắm trại mới qua API
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Camping> addCamping(@RequestBody Camping camping, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden if not authenticated
        }
        String username = authentication.getName();
        return ResponseEntity.ok(campingService.createCamping(camping, username));
    }

    // Cập nhật khu cắm trại qua API
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Camping> updateCamping(@PathVariable("id") Long id, @RequestBody Camping campingDetails) {
        Camping updatedCamping = campingService.updateCamping(id, campingDetails);
        return updatedCamping != null ? ResponseEntity.ok(updatedCamping) : ResponseEntity.notFound().build();
    }

    // Xóa khu cắm trại qua API
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCamping(@PathVariable("id") Long id) {
        campingService.deleteCamping(id);
        return ResponseEntity.noContent().build();
    }

    // Đặt chỗ khu cắm trại
    @PostMapping("/book/{id}")
    public String bookCamping(
            @PathVariable("id") Long id,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam int numberOfPeople,
            @RequestParam String customerName,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để đặt chỗ!");
            return "redirect:/camping/" + id;
        }
        
        boolean booked = campingService.bookCamping(id, LocalDate.parse(startDate), LocalDate.parse(endDate), numberOfPeople, customerName);
        if (booked) {
            redirectAttributes.addFlashAttribute("message", "Đặt chỗ thành công!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không thể đặt chỗ!");
        }
        return "redirect:/camping";
    }

    // Hủy đặt chỗ khu cắm trại
    @PostMapping("/cancel/{id}")
    public String cancelBooking(
            @PathVariable("id") Long id,
            @RequestParam int bookingIndex,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để hủy đặt chỗ!");
            return "redirect:/camping/" + id;
        }
        
        boolean cancelled = campingService.cancelBooking(id, bookingIndex);
        if (cancelled) {
            redirectAttributes.addFlashAttribute("message", "Hủy đặt chỗ thành công!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không thể hủy!");
        }
        return "redirect:/camping";
    }

    // Thêm đánh giá mới qua API
    @PostMapping("/api/{id}/review")
    @ResponseBody
    public ResponseEntity<Camping> addReview(
            @PathVariable("id") Long id,
            @RequestParam Integer rating,
            @RequestParam String comment,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        Camping camping = campingService.addReview(id, rating, comment, null);
        return ResponseEntity.ok(camping);
    }

    // Phản hồi một đánh giá qua API
    @PutMapping("/api/{id}/review/{reviewIndex}/reply")
    @ResponseBody
    public ResponseEntity<Camping> replyToReview(
            @PathVariable("id") Long id,
            @PathVariable("reviewIndex") int reviewIndex,
            @RequestParam String reply,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        Camping camping = campingService.replyToReview(id, reviewIndex, reply);
        return ResponseEntity.ok(camping);
    }

    // Lấy danh sách đánh giá qua API
    @GetMapping("/api/{id}/reviews")
    @ResponseBody
    public ResponseEntity<List<String>> getReviews(@PathVariable("id") Long id) {
        Optional<Camping> camping = campingService.getCampingById(id);
        return camping.map(c -> ResponseEntity.ok(c.getReviews()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Lấy danh sách thông báo qua API
    @GetMapping("/api/{id}/notifications")
    @ResponseBody
    public ResponseEntity<List<String>> getNotifications(@PathVariable("id") Long id) {
        Optional<Camping> camping = campingService.getCampingById(id);
        return camping.map(c -> ResponseEntity.ok(c.getRecentNotifications(5)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Thêm thông báo mới qua API
    @PostMapping("/api/{id}/notify")
    @ResponseBody
    public ResponseEntity<Camping> addNotification(
            @PathVariable("id") Long id,
            @RequestParam String message,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        Camping camping = campingService.addNotification(id, message);
        return ResponseEntity.ok(camping);
    }

    // Lấy danh sách gợi ý qua API
    @GetMapping("/api/{id}/suggestions")
    @ResponseBody
    public ResponseEntity<List<String>> getSuggestions(@PathVariable("id") Long id) {
        Optional<Camping> camping = campingService.getCampingById(id);
        return camping.map(c -> ResponseEntity.ok(c.getSuggestedTerms()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Thêm từ khóa tìm kiếm qua API
    @PostMapping("/api/{id}/search")
    @ResponseBody
    public ResponseEntity<Camping> addSearchTerm(
            @PathVariable("id") Long id,
            @RequestParam String term,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        Camping camping = campingService.addSearchTerm(id, term);
        return ResponseEntity.ok(camping);
    }

    // Xử lý thanh toán qua API
    @PostMapping("/api/{id}/pay")
    @ResponseBody
    public ResponseEntity<String> processPayment(
            @PathVariable("id") Long id,
            @RequestParam Double amount,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        String paymentStatus = campingService.processPayment(id, amount);
        return ResponseEntity.ok(paymentStatus);
    }

    // API tìm kiếm khu cắm trại gần vị trí hiện tại
    @GetMapping("/api/nearby")
    @ResponseBody
    public ResponseEntity<List<Camping>> findCampingsNearby(
            @RequestParam("lat") Double latitude,
            @RequestParam("lng") Double longitude,
            @RequestParam(value = "radius", defaultValue = "10.0") Double radiusInKm) {
        try {
            List<Camping> campings = campingService.findCampingsNearby(latitude, longitude, radiusInKm);
            return ResponseEntity.ok(campings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API tìm kiếm nâng cao
    @GetMapping("/api/search/advanced")
    @ResponseBody
    public ResponseEntity<List<Camping>> searchAdvanced(
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "minRating", required = false) Double minRating) {
        try {
            Camping.CampingStatus status = null;
            if (statusStr != null && !statusStr.isEmpty()) {
                try {
                    status = Camping.CampingStatus.valueOf(statusStr);
                } catch (IllegalArgumentException e) {
                    // Bỏ qua giá trị không hợp lệ và tiếp tục tìm kiếm
                }
            }
            
            List<Camping> campings = campingService.searchCampingsAdvanced(
                location, minPrice, maxPrice, status, minRating);
            return ResponseEntity.ok(campings);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API tìm kiếm theo từ khóa
    @GetMapping("/api/search/keyword")
    @ResponseBody
    public ResponseEntity<List<Camping>> searchByKeyword(
            @RequestParam("keyword") String keyword) {
        try {
            List<Camping> campings = campingService.searchByKeyword(keyword);
            return ResponseEntity.ok(campings);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API tìm kiếm theo mùa
    @GetMapping("/api/search/season")
    @ResponseBody
    public ResponseEntity<List<Camping>> findByBestSeason(
            @RequestParam("season") String season) {
        try {
            List<Camping> campings = campingService.findByBestSeason(season);
            return ResponseEntity.ok(campings);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API tìm kiếm theo mức độ tiếp cận
    @GetMapping("/api/search/accessibility")
    @ResponseBody
    public ResponseEntity<List<Camping>> findByAccessibilityLevel(
            @RequestParam("maxLevel") Integer maxLevel) {
        try {
            List<Camping> campings = campingService.findByAccessibilityLevel(maxLevel);
            return ResponseEntity.ok(campings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API lấy danh sách khu cắm trại đang mở theo đánh giá
    @GetMapping("/api/open")
    @ResponseBody
    public ResponseEntity<List<Camping>> getAllOpenCampings() {
        try {
            List<Camping> campings = campingService.getAllOpenCampingsOrderByRating();
            return ResponseEntity.ok(campings);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API lấy danh sách khu cắm trại cập nhật gần đây
    @GetMapping("/api/recent")
    @ResponseBody
    public ResponseEntity<List<Camping>> getRecentlyUpdatedCampings(
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        try {
            List<Camping> campings = campingService.getRecentlyUpdatedCampings(days);
            return ResponseEntity.ok(campings);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API lấy danh sách khu cắm trại có video
    @GetMapping("/api/with-video")
    @ResponseBody
    public ResponseEntity<List<Camping>> getCampingsWithVideo() {
        try {
            List<Camping> campings = campingService.getCampingsWithVideo();
            return ResponseEntity.ok(campings);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API cập nhật tọa độ GPS
    @PutMapping("/api/{id}/coordinates")
    @ResponseBody
    public ResponseEntity<Camping> updateGpsCoordinates(
            @PathVariable("id") Long id,
            @RequestParam("lat") Double latitude,
            @RequestParam("lng") Double longitude,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            Camping camping = campingService.updateGpsCoordinates(id, latitude, longitude);
            return ResponseEntity.ok(camping);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API cập nhật giá theo mùa
    @PutMapping("/api/{id}/seasonal-prices")
    @ResponseBody
    public ResponseEntity<Camping> updateSeasonalPrices(
            @PathVariable("id") Long id,
            @RequestParam(value = "standardPrice", required = false) Double standardPrice,
            @RequestParam(value = "peakSeasonPrice", required = false) Double peakSeasonPrice,
            @RequestParam(value = "lowSeasonPrice", required = false) Double lowSeasonPrice,
            @RequestParam(value = "weekendPrice", required = false) Double weekendPrice,
            @RequestParam(value = "peakSeasonInfo", required = false) String peakSeasonInfo,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            Camping camping = campingService.updateSeasonalPrices(
                id, standardPrice, peakSeasonPrice, lowSeasonPrice, weekendPrice, peakSeasonInfo);
            return ResponseEntity.ok(camping);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API cập nhật trạng thái
    @PutMapping("/api/{id}/status")
    @ResponseBody
    public ResponseEntity<Camping> updateCampingStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String statusStr,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            Camping.CampingStatus status = Camping.CampingStatus.valueOf(statusStr);
            Camping camping = campingService.updateCampingStatus(id, status);
            return ResponseEntity.ok(camping);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API thêm hoạt động giải trí
    @PostMapping("/api/{id}/activity")
    @ResponseBody
    public ResponseEntity<Camping> addActivity(
            @PathVariable("id") Long id,
            @RequestParam("activity") String activity,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            Camping camping = campingService.addActivity(id, activity);
            return ResponseEntity.ok(camping);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API thêm điểm tham quan lân cận
    @PostMapping("/api/{id}/attraction")
    @ResponseBody
    public ResponseEntity<Camping> addNearbyAttraction(
            @PathVariable("id") Long id,
            @RequestParam("attraction") String attraction,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            Camping camping = campingService.addNearbyAttraction(id, attraction);
            return ResponseEntity.ok(camping);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API cập nhật URL video
    @PutMapping("/api/{id}/video")
    @ResponseBody
    public ResponseEntity<Camping> updateVideoUrl(
            @PathVariable("id") Long id,
            @RequestParam("videoUrl") String videoUrl,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            Camping camping = campingService.updateVideoUrl(id, videoUrl);
            return ResponseEntity.ok(camping);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API cập nhật thông tin tiếp cận
    @PutMapping("/api/{id}/accessibility")
    @ResponseBody
    public ResponseEntity<Camping> updateAccessibilityInfo(
            @PathVariable("id") Long id,
            @RequestParam(value = "level", required = false) Integer level,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            Camping camping = campingService.updateAccessibilityInfo(id, level, description);
            return ResponseEntity.ok(camping);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API cập nhật thông tin an toàn
    @PutMapping("/api/{id}/safety")
    @ResponseBody
    public ResponseEntity<Camping> updateSafetyInfo(
            @PathVariable("id") Long id,
            @RequestParam("safetyInfo") String safetyInfo,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            Camping camping = campingService.updateSafetyInfo(id, safetyInfo);
            return ResponseEntity.ok(camping);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // API cập nhật thông tin mùa tốt nhất
    @PutMapping("/api/{id}/best-seasons")
    @ResponseBody
    public ResponseEntity<Camping> updateBestSeasons(
            @PathVariable("id") Long id,
            @RequestParam("bestSeasons") String bestSeasons,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        try {
            Camping camping = campingService.updateBestSeasons(id, bestSeasons);
            return ResponseEntity.ok(camping);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}