package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Review;
import ut.edu.project.models.Travel;
import ut.edu.project.models.User;
import ut.edu.project.services.BookingService;
import ut.edu.project.services.ReviewService;
import ut.edu.project.services.TravelService;
import ut.edu.project.services.UserService;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/travels") // Đường dẫn chính của trang du lịch
public class TravelController {

    private static final Logger log = LoggerFactory.getLogger(TravelController.class);

    @Autowired  
    private TravelService travelService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showTravelList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            Model model,
            @Autowired(required = false) CsrfToken csrfToken) {
        
        try {
            List<Travel> travels = travelService.searchAndFilterTravels(
                search, location, minPrice, maxPrice, duration, minRating, sortBy, sortOrder
            );
            
            model.addAttribute("travels", travels);
            model.addAttribute("currentPage", "travels");
            
            // Tạo Map với các giá trị mặc định cho các tham số null
            model.addAttribute("searchParams", Map.of(
                "search", search != null ? search : "",
                "location", location != null ? location : "",
                "minPrice", minPrice != null ? minPrice : 0.0,
                "maxPrice", maxPrice != null ? maxPrice : 0.0,
                "duration", duration != null ? duration : 0,
                "minRating", minRating != null ? minRating : 0.0,
                "sortBy", sortBy != null ? sortBy : "",
                "sortOrder", sortOrder != null ? sortOrder : ""
            ));

            if (csrfToken != null) {
                model.addAttribute("_csrf", csrfToken);
            }
            
            return "travel/travel"; // Điều hướng đến file templates/travel/travel.html
        } catch (Exception e) {
            log.error("Error loading travel list: {}", e.getMessage(), e);
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách tour. Vui lòng thử lại sau.");
            return "error/error";
        }
    }

    // Method for travel details with reviews and booking status
    @GetMapping("/{id}")
    public String getTravelById(@PathVariable("id") Long id, Model model, Authentication authentication,
                              @Autowired(required = false) CsrfToken csrfToken) {
        try {
            Travel travel = travelService.getTravelById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tour với ID: " + id));

            // Get reviews for this travel
            List<Review> reviews = reviewService.getReviewsByTravel(id);
            Double averageRating = reviewService.getAverageRatingByTravel(id);
            Long reviewCount = reviewService.countReviewsByTravel(id);

            // Check if user has booked this travel and can review
            boolean hasBookedTravel = false;
            boolean hasReviewed = false;
            boolean isInWishlist = false;

            if (authentication != null) {
                String username = authentication.getName();
                hasBookedTravel = bookingService.hasUserBookedTravel(username, id);
                hasReviewed = reviewService.hasUserReviewedTravel(username, id);
                isInWishlist = travelService.isTravelInWishlist(username, id);
            }

            model.addAttribute("travel", travel);
            model.addAttribute("reviews", reviews);
            model.addAttribute("averageRating", averageRating);
            model.addAttribute("reviewCount", reviewCount);
            model.addAttribute("hasBookedTravel", hasBookedTravel);
            model.addAttribute("hasReviewed", hasReviewed);
            model.addAttribute("isInWishlist", isInWishlist);
            model.addAttribute("currentPage", "travels");
            model.addAttribute("shareCount", travelService.getShareCount(id));
            model.addAttribute("sharedBy", travelService.getSharedBy(id));

            if (csrfToken != null) {
                model.addAttribute("_csrf", csrfToken);
            }

            return "travel/travel-detail";
        } catch (Exception e) {
            log.error("Error loading travel details: {}", e.getMessage(), e);
            model.addAttribute("error", "Có lỗi xảy ra khi tải thông tin tour. Vui lòng thử lại sau.");
            return "error/error";
        }
    }

    @PostMapping("/{id}/wishlist")
    @ResponseBody
    public String toggleWishlist(@PathVariable("id") Long id, Authentication authentication) {
        if (authentication == null) {
            return "{\"error\": \"Vui lòng đăng nhập trước\"}";
        }
        try {
            String username = authentication.getName();
            boolean isAdded = travelService.toggleWishlist(username, id);
            return "{\"success\": true, \"isInWishlist\": " + isAdded + "}";
        } catch (Exception e) {
            log.error("Error toggling wishlist: {}", e.getMessage(), e);
            return "{\"error\": \"Có lỗi xảy ra. Vui lòng thử lại sau.\"}";
        }
    }

    @GetMapping("/compare")
    public String compareTravels(@RequestParam List<Long> ids, Model model) {
        try {
            List<Travel> travels = travelService.getTravelsByIds(ids);
            model.addAttribute("travels", travels);
            return "travel/travel-compare";
        } catch (Exception e) {
            log.error("Error comparing travels: {}", e.getMessage(), e);
            model.addAttribute("error", "Có lỗi xảy ra khi so sánh tour. Vui lòng thử lại sau.");
            return "error/error";
        }
    }

    @PostMapping("/{id}/share")
    @ResponseBody
    public String shareTravel(@PathVariable("id") Long id, @RequestParam String email) {
        try {
            travelService.shareTravel(id, email);
            return "{\"success\": true, \"shareCount\": " + travelService.getShareCount(id) + "}";
        } catch (Exception e) {
            log.error("Error sharing travel: {}", e.getMessage(), e);
            return "{\"error\": \"Có lỗi xảy ra khi chia sẻ tour. Vui lòng thử lại sau.\"}";
        }
    }

    @PostMapping("/{id}/reviews")
    public String addReview(@PathVariable("id") Long id,
                          @RequestParam("rating") int rating,
                          @RequestParam("comment") String comment,
                          Authentication authentication,
                          Model model) {
        try {
            if (authentication == null) {
                model.addAttribute("error", "Vui lòng đăng nhập để đánh giá");
                return "error/error";
            }

            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            if (user == null) {
                model.addAttribute("error", "Không tìm thấy người dùng");
                return "error/error";
            }

            if (!bookingService.hasUserBookedTravel(username, id)) {
                model.addAttribute("error", "Bạn cần đặt tour trước khi đánh giá");
                return "error/error";
            }

            // Kiểm tra xem người dùng đã đánh giá chưa
            List<Review> userReviews = reviewService.getReviewsByUser(user);
            boolean hasReviewed = userReviews.stream()
                .anyMatch(review -> review.getTravel().getId().equals(id));
            
            if (hasReviewed) {
                model.addAttribute("error", "Bạn đã đánh giá tour này rồi");
                return "error/error";
            }

            Review review = new Review();
            review.setRating(rating);
            review.setComment(comment);
            review.setUser(user);
            review.setTravel(travelService.getTravelById(id).orElseThrow());

            reviewService.createReview(review);
            return "redirect:/travels/" + id;
        } catch (Exception e) {
            log.error("Error adding review: {}", e.getMessage(), e);
            model.addAttribute("error", "Có lỗi xảy ra khi thêm đánh giá. Vui lòng thử lại sau.");
            return "error/error";
        }
    }

    @PostMapping("/{id}/reviews/{reviewId}/edit")
    public String editReview(@PathVariable("id") Long id,
                           @PathVariable("reviewId") Long reviewId,
                           @RequestParam("rating") int rating,
                           @RequestParam("comment") String comment,
                           Authentication authentication,
                           Model model) {
        try {
            if (authentication == null) {
                model.addAttribute("error", "Vui lòng đăng nhập để chỉnh sửa đánh giá");
                return "error/error";
            }

            String username = authentication.getName();
            Review review = reviewService.getReviewById(reviewId);
            
            if (!review.getUser().getUsername().equals(username)) {
                model.addAttribute("error", "Bạn không có quyền chỉnh sửa đánh giá này");
                return "error/error";
            }

            review.setRating(rating);
            review.setComment(comment);
            reviewService.updateReview(reviewId, review);
            
            return "redirect:/travels/" + id;
        } catch (Exception e) {
            log.error("Error editing review: {}", e.getMessage(), e);
            model.addAttribute("error", "Có lỗi xảy ra khi chỉnh sửa đánh giá. Vui lòng thử lại sau.");
            return "error/error";
        }
    }

    @PostMapping("/{id}/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable("id") Long id,
                             @PathVariable("reviewId") Long reviewId,
                             Authentication authentication,
                             Model model) {
        try {
            if (authentication == null) {
                model.addAttribute("error", "Vui lòng đăng nhập để xóa đánh giá");
                return "error/error";
            }

            String username = authentication.getName();
            Review review = reviewService.getReviewById(reviewId);
            
            if (!review.getUser().getUsername().equals(username)) {
                model.addAttribute("error", "Bạn không có quyền xóa đánh giá này");
                return "error/error";
            }

            reviewService.deleteReview(reviewId);
            return "redirect:/travels/" + id;
        } catch (Exception e) {
            log.error("Error deleting review: {}", e.getMessage(), e);
            model.addAttribute("error", "Có lỗi xảy ra khi xóa đánh giá. Vui lòng thử lại sau.");
            return "error/error";
        }
    }

    @PostMapping("/{id}/reviews/{reviewId}/like")
    @ResponseBody
    public String likeReview(@PathVariable("id") Long id,
                           @PathVariable("reviewId") Long reviewId,
                           Authentication authentication) {
        try {
            if (authentication == null) {
                return "{\"error\": \"Vui lòng đăng nhập để thích đánh giá\"}";
            }

            String username = authentication.getName();
            boolean isLiked = reviewService.toggleLikeReview(reviewId, username);
            return "{\"success\": true, \"isLiked\": " + isLiked + "}";
        } catch (Exception e) {
            log.error("Error liking review: {}", e.getMessage(), e);
            return "{\"error\": \"Có lỗi xảy ra khi thích đánh giá. Vui lòng thử lại sau.\"}";
        }
    }

    @PostMapping("/{id}/reviews/{reviewId}/report")
    @ResponseBody
    public String reportReview(@PathVariable("id") Long id,
                             @PathVariable("reviewId") Long reviewId,
                             @RequestParam("reason") String reason,
                             Authentication authentication) {
        try {
            if (authentication == null) {
                return "{\"error\": \"Vui lòng đăng nhập để báo cáo đánh giá\"}";
            }

            String username = authentication.getName();
            reviewService.reportReview(reviewId, username, reason);
            return "{\"success\": true}";
        } catch (Exception e) {
            log.error("Error reporting review: {}", e.getMessage(), e);
            return "{\"error\": \"Có lỗi xảy ra khi báo cáo đánh giá. Vui lòng thử lại sau.\"}";
        }
    }
}
