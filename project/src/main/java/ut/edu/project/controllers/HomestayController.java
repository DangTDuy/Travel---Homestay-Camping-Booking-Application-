package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ut.edu.project.models.Homestay;
import ut.edu.project.services.HomestayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.edu.project.models.Review;
import ut.edu.project.services.ReviewService;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import ut.edu.project.dtos.AdditionalDTO;
import ut.edu.project.services.AdditionalService;
import ut.edu.project.models.User;
import ut.edu.project.models.Booking;
import ut.edu.project.services.UserService;
import ut.edu.project.services.BookingService;

@Controller
@RequestMapping("/homestay")
public class HomestayController {

    private static final Logger log = LoggerFactory.getLogger(HomestayController.class);

    @Autowired
    private HomestayService homestayService;

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private AdditionalService additionalService;

    @GetMapping
    public String getAllHomestays(Model model,
                                @RequestParam(required = false) String location,
                                @RequestParam(required = false) String priceRange,
                                @RequestParam(required = false) String rating) {
        try {
            log.info("Getting all homestays for user with filters - location: {}, priceRange: {}, rating: {}",
                    location, priceRange, rating);
            List<Homestay> homestays = homestayService.searchHomestays(location, priceRange, rating);
            log.info("Found {} homestays", homestays.size());

            model.addAttribute("homestays", homestays);
            model.addAttribute("location", location);
            model.addAttribute("priceRange", priceRange);
            model.addAttribute("rating", rating);
            model.addAttribute("currentPage", "homestay");
            return "homestay/homestay";
        } catch (Exception e) {
            log.error("Error getting homestays: {}", e.getMessage(), e);
            model.addAttribute("error", "Không thể tải danh sách homestay: " + e.getMessage());
            model.addAttribute("currentPage", "homestay");
            return "homestay/homestay";
        }
    }

    @GetMapping("/{id}")
    public String getHomestayById(@PathVariable Long id, Model model, CsrfToken csrfToken, Authentication authentication) {
        try {
            Homestay homestay = homestayService.getHomestayById(id)
                    .orElseThrow(() -> new RuntimeException("Homestay not found"));

            // Lấy danh sách đánh giá cho homestay này
            List<Review> reviews = reviewService.getReviewsByHomestay(id);
            Double averageRating = reviewService.getAverageRatingByHomestay(id);
            Long reviewCount = reviewService.countReviewsByHomestay(id);

            // Kiểm tra nếu người dùng đã đăng nhập, thì thêm thông tin đã đặt phòng chưa
            boolean hasBookedHomestay = false;
            boolean canReview = false;
            boolean isOwner = false;
            Long bookingId = null;
            
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                User user = userService.findByUsername(username).orElse(null);
                
                if (user != null) {
                    // Kiểm tra xem người dùng có phải là chủ homestay không
                    isOwner = homestay.getOwner() != null && homestay.getOwner().getId() != null && 
                            user.getId() != null && homestay.getOwner().getId().equals(user.getId());
                    
                    // Kiểm tra xem người dùng có booking nào đã hoàn thành và chưa đánh giá không
                    Booking completedBooking = bookingService.findCompletedBookingForReview(user.getId(), id);
                    if (completedBooking != null) {
                        canReview = true;
                        bookingId = completedBooking.getId();
                    }
                    
                    hasBookedHomestay = homestayService.hasUserBookedHomestay(username, id);
                    log.info("User {} has booked homestay {}: {}", username, id, hasBookedHomestay);
                }
            }

            // Lấy danh sách dịch vụ bổ sung cho homestay
            List<AdditionalDTO> additionalServices = additionalService.getByHomestay(homestay).stream()
                    .map(additionalService::convertToDTO)
                    .collect(java.util.stream.Collectors.toList());
                    
            // Nhóm các dịch vụ bổ sung theo danh mục
            Map<String, List<AdditionalDTO>> servicesByCategory = additionalServices.stream()
                    .filter(service -> service.getCategory() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                        service -> service.getCategory().getDisplayName() != null ? 
                                service.getCategory().getDisplayName() : "Khác"
                    ));
                    
            model.addAttribute("homestay", homestay);
            model.addAttribute("reviews", reviews);
            model.addAttribute("currentPage", "homestay");
            model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
            model.addAttribute("reviewCount", reviewCount);
            model.addAttribute("hasBookedHomestay", hasBookedHomestay);
            model.addAttribute("canReview", canReview);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("bookingId", bookingId);
            model.addAttribute("additionalServices", additionalServices);
            model.addAttribute("servicesByCategory", servicesByCategory);

            if (csrfToken != null) {
                model.addAttribute("_csrf", csrfToken);
            }
            return "homestay/homestay-detail";
        } catch (Exception e) {
            log.error("Error loading homestay details: {}", e.getMessage(), e);
            return "redirect:/error?message=" + e.getMessage();
        }
    }

    @GetMapping("/{id}/book")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String bookHomestay(@PathVariable Long id, Model model) {
        Homestay homestay = homestayService.getHomestayById(id)
                .orElseThrow(() -> new RuntimeException("Homestay not found"));
        model.addAttribute("homestay", homestay);
        model.addAttribute("currentPage", "homestay");
        return "booking/booking-form";
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String uploadImages(@PathVariable Long id,
                             @RequestParam("images") MultipartFile[] images) throws IOException {
        homestayService.uploadImages(id, images);
        return "redirect:/homestay/" + id;
    }

    @GetMapping("/{id}/review")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String showReviewForm(@PathVariable Long id, Model model, Authentication authentication) {
        log.info("Showing review form for homestay id: {}", id);

        Homestay homestay = homestayService.getHomestayById(id)
                .orElseThrow(() -> new RuntimeException("Homestay not found"));

        String username = authentication.getName();
        // Kiểm tra xem người dùng đã từng đặt homestay này chưa
        boolean hasBookedHomestay = homestayService.hasUserBookedHomestay(username, id);

        if (!hasBookedHomestay) {
            log.warn("User {} tried to review homestay {} without booking", username, id);
            return "redirect:/error?message=Bạn cần phải đặt và sử dụng dịch vụ trước khi đánh giá";
        }

        model.addAttribute("homestay", homestay);
        model.addAttribute("review", new Review());
        model.addAttribute("currentPage", "homestay");
        return "homestay/review-form";
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String submitReview(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam String comment,
            @RequestParam(required = false) MultipartFile[] images,
            Authentication authentication) {

        log.info("Submitting review for homestay id: {} with rating: {}", id, rating);

        String username = authentication.getName();
        // Kiểm tra xem người dùng đã từng đặt homestay này chưa
        boolean hasBookedHomestay = homestayService.hasUserBookedHomestay(username, id);

        if (!hasBookedHomestay) {
            log.warn("User {} tried to review homestay {} without booking", username, id);
            return "redirect:/error?message=Bạn cần phải đặt và sử dụng dịch vụ trước khi đánh giá";
        }

        try {
            Review review = reviewService.createReview(id, username, rating, comment);

            // Xử lý upload ảnh nếu có
            if (images != null && images.length > 0) {
                List<String> imageUrls = homestayService.uploadReviewImages(id, images);
                review.setImages(imageUrls);
                reviewService.updateReview(review.getId(), review);
            }

            log.info("Review created successfully with id: {}", review.getId());
            return "redirect:/homestay/" + id;
        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage(), e);
            return "redirect:/error?message=Không thể tạo đánh giá: " + e.getMessage();
        }
    }
}