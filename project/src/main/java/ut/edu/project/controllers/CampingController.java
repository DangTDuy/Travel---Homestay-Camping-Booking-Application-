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
    public ResponseEntity<Camping> addCamping(@RequestBody Camping camping) {
        return ResponseEntity.ok(campingService.createCamping(camping));
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
            RedirectAttributes redirectAttributes) {
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
            RedirectAttributes redirectAttributes) {
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
            @RequestParam String comment) {
        Camping camping = campingService.addReview(id, rating, comment, null);
        return ResponseEntity.ok(camping);
    }

    // Phản hồi một đánh giá qua API
    @PutMapping("/api/{id}/review/{reviewIndex}/reply")
    @ResponseBody
    public ResponseEntity<Camping> replyToReview(
            @PathVariable("id") Long id,
            @PathVariable("reviewIndex") int reviewIndex,
            @RequestParam String reply) {
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
            @RequestParam String message) {
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
            @RequestParam String term) {
        Camping camping = campingService.addSearchTerm(id, term);
        return ResponseEntity.ok(camping);
    }

    // Xử lý thanh toán qua API
    @PostMapping("/api/{id}/pay")
    @ResponseBody
    public ResponseEntity<String> processPayment(
            @PathVariable("id") Long id,
            @RequestParam Double amount) {
        String paymentStatus = campingService.processPayment(id, amount);
        return ResponseEntity.ok(paymentStatus);
    }
}