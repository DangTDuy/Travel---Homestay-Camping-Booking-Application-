package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.Review;
import ut.edu.project.models.User;
import ut.edu.project.services.HomestayService;
import ut.edu.project.services.ReviewService;
import ut.edu.project.services.UserService;

import java.util.List;

@Controller
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public String createReview(
            @RequestParam Long homestayId,
            @RequestParam Integer rating,
            @RequestParam String comment,
            Authentication authentication) {
        
        String username = authentication.getName();
        reviewService.createReview(homestayId, username, rating, comment);
        
        return "redirect:/homestay/" + homestayId;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        Review review = reviewService.getReviewById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Kiểm tra quyền sửa đánh giá
        if (!review.getUser().getUsername().equals(authentication.getName())) {
            return "redirect:/error";
        }
        
        model.addAttribute("review", review);
        return "review/edit-review";
    }

    @PostMapping("/update/{id}")
    public String updateReview(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam String comment,
            Authentication authentication) {
        
        Review review = reviewService.getReviewById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Kiểm tra quyền sửa đánh giá
        if (!review.getUser().getUsername().equals(authentication.getName())) {
            return "redirect:/error";
        }
        
        review.setRating(rating);
        review.setComment(comment);
        reviewService.updateReview(id, review);
        
        return "redirect:/homestay/" + review.getHomestay().getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id, Authentication authentication) {
        Review review = reviewService.getReviewById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Kiểm tra quyền xóa đánh giá
        if (!review.getUser().getUsername().equals(authentication.getName())) {
            return "redirect:/error";
        }
        
        Long homestayId = review.getHomestay().getId();
        reviewService.deleteReview(id);
        
        return "redirect:/homestay/" + homestayId;
    }

    // Lấy review theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá")));
    }

    // Lấy review của người dùng
    @GetMapping("/user")
    public ResponseEntity<List<Review>> getReviewsByUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(reviewService.getReviewsByUser(user));
    }

    // Lấy review của homestay
    @GetMapping("/homestay/{homestayId}")
    public ResponseEntity<List<Review>> getReviewsByHomestay(@PathVariable Long homestayId) {
        return ResponseEntity.ok(reviewService.getReviewsByHomestay(homestayId));
    }

    // Lấy review của camping
    @GetMapping("/camping/{campingId}")
    public ResponseEntity<List<Review>> getReviewsByCamping(@PathVariable Long campingId) {
        return ResponseEntity.ok(reviewService.getReviewsByCamping(campingId));
    }

    // Lấy review của travel
    @GetMapping("/travel/{travelId}")
    public ResponseEntity<List<Review>> getReviewsByTravel(@PathVariable Long travelId) {
        return ResponseEntity.ok(reviewService.getReviewsByTravel(travelId));
    }

    // Tìm kiếm review
    @GetMapping("/search")
    public ResponseEntity<List<Review>> searchReviews(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long homestayId,
            @RequestParam(required = false) Long campingId,
            @RequestParam(required = false) Long travelId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating) {
        return ResponseEntity.ok(reviewService.searchReviews(
                userId, homestayId, campingId, travelId, minRating, maxRating));
    }

    // Lấy review mới nhất
    @GetMapping("/recent")
    public ResponseEntity<List<Review>> getRecentReviews() {
        return ResponseEntity.ok(reviewService.getRecentReviews());
    }

    // Lấy điểm trung bình của homestay
    @GetMapping("/homestay/{homestayId}/rating")
    public ResponseEntity<Double> getHomestayRating(@PathVariable Long homestayId) {
        return ResponseEntity.ok(reviewService.getAverageRatingByHomestay(homestayId));
    }

    // Lấy điểm trung bình của camping
    @GetMapping("/camping/{campingId}/rating")
    public ResponseEntity<Double> getCampingRating(@PathVariable Long campingId) {
        return ResponseEntity.ok(reviewService.getAverageRatingByCamping(campingId));
    }

    // Lấy điểm trung bình của travel
    @GetMapping("/travel/{travelId}/rating")
    public ResponseEntity<Double> getTravelRating(@PathVariable Long travelId) {
        return ResponseEntity.ok(reviewService.getAverageRatingByTravel(travelId));
    }

    // Lấy số lượng review của homestay
    @GetMapping("/homestay/{homestayId}/count")
    public ResponseEntity<Long> getHomestayReviewCount(@PathVariable Long homestayId) {
        return ResponseEntity.ok(reviewService.countReviewsByHomestay(homestayId));
    }

    // Lấy số lượng review của camping
    @GetMapping("/camping/{campingId}/count")
    public ResponseEntity<Long> getCampingReviewCount(@PathVariable Long campingId) {
        return ResponseEntity.ok(reviewService.countReviewsByCamping(campingId));
    }

    // Lấy số lượng review của travel
    @GetMapping("/travel/{travelId}/count")
    public ResponseEntity<Long> getTravelReviewCount(@PathVariable Long travelId) {
        return ResponseEntity.ok(reviewService.countReviewsByTravel(travelId));
    }
}