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
import ut.edu.project.dtos.ReviewRequestDTO;
import ut.edu.project.dtos.ReviewReplyRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/review")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

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

        try {
            String username = authentication.getName();
            Review review = reviewService.createReview(homestayId, username, rating, comment);

            return "redirect:/homestay/" + homestayId;
        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage(), e);
            return "redirect:/error?message=" + e.getMessage();
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, Authentication authentication) {
        try {
            Review review = reviewService.getReviewById(id);

            // Kiểm tra quyền sửa đánh giá
            if (!review.getUser().getUsername().equals(authentication.getName())) {
                return "redirect:/error";
            }

            model.addAttribute("review", review);
            return "review/edit-review";
        } catch (RuntimeException e) {
            return "redirect:/error?message=" + e.getMessage();
        }
    }

    @PostMapping("/update/{id}")
    public String updateReview(
            @PathVariable("id") Long id,
            @RequestParam Integer rating,
            @RequestParam String comment,
            Authentication authentication) {

        try {
            Review review = reviewService.getReviewById(id);

            // Kiểm tra quyền sửa đánh giá
            if (!review.getUser().getUsername().equals(authentication.getName())) {
                return "redirect:/error";
            }

            review.setRating(rating);
            review.setComment(comment);
            reviewService.updateReview(id, review);

            return "redirect:/homestay/" + review.getHomestay().getId();
        } catch (RuntimeException e) {
            return "redirect:/error?message=" + e.getMessage();
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable("id") Long id, Authentication authentication) {
        try {
            Review review = reviewService.getReviewById(id);

            // Kiểm tra quyền xóa đánh giá
            if (!review.getUser().getUsername().equals(authentication.getName())) {
                return "redirect:/error";
            }

            Long homestayId = review.getHomestay().getId();
            reviewService.deleteReview(id);

            return "redirect:/homestay/" + homestayId;
        } catch (RuntimeException e) {
            return "redirect:/error?message=" + e.getMessage();
        }
    }

    // Lấy review theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(reviewService.getReviewById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
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
    public ResponseEntity<List<Review>> getReviewsByHomestay(@PathVariable("homestayId") Long homestayId) {
        return ResponseEntity.ok(reviewService.getReviewsByHomestay(homestayId));
    }

    // Lấy review của camping
    @GetMapping("/camping/{campingId}")
    public ResponseEntity<List<Review>> getReviewsByCamping(@PathVariable("campingId") Long campingId) {
        return ResponseEntity.ok(reviewService.getReviewsByCamping(campingId));
    }

    // Lấy review của travel
    @GetMapping("/travel/{travelId}")
    public ResponseEntity<List<Review>> getReviewsByTravel(@PathVariable("travelId") Long travelId) {
        return ResponseEntity.ok(reviewService.getReviewsByTravel(travelId));
    }

    // Tìm kiếm review
    @GetMapping("/search")
    public ResponseEntity<List<Review>> searchReviews(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "homestayId", required = false) Long homestayId,
            @RequestParam(name = "campingId", required = false) Long campingId,
            @RequestParam(name = "travelId", required = false) Long travelId,
            @RequestParam(name = "minRating", required = false) Integer minRating,
            @RequestParam(name = "maxRating", required = false) Integer maxRating) {
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
    public ResponseEntity<Double> getHomestayRating(@PathVariable("homestayId") Long homestayId) {
        return ResponseEntity.ok(reviewService.getAverageRatingByHomestay(homestayId));
    }

    // Lấy điểm trung bình của camping
    @GetMapping("/camping/{campingId}/rating")
    public ResponseEntity<Double> getCampingRating(@PathVariable("campingId") Long campingId) {
        return ResponseEntity.ok(reviewService.getAverageRatingByCamping(campingId));
    }

    // Lấy điểm trung bình của travel
    @GetMapping("/travel/{travelId}/rating")
    public ResponseEntity<Double> getTravelRating(@PathVariable("travelId") Long travelId) {
        return ResponseEntity.ok(reviewService.getAverageRatingByTravel(travelId));
    }

    // Lấy số lượng review của homestay
    @GetMapping("/homestay/{homestayId}/count")
    public ResponseEntity<Long> getHomestayReviewCount(@PathVariable("homestayId") Long homestayId) {
        return ResponseEntity.ok(reviewService.countReviewsByHomestay(homestayId));
    }

    // Lấy số lượng review của camping
    @GetMapping("/camping/{campingId}/count")
    public ResponseEntity<Long> getCampingReviewCount(@PathVariable("campingId") Long campingId) {
        return ResponseEntity.ok(reviewService.countReviewsByCamping(campingId));
    }

    // Lấy số lượng review của travel
    @GetMapping("/travel/{travelId}/count")
    public ResponseEntity<Long> getTravelReviewCount(@PathVariable("travelId") Long travelId) {
        return ResponseEntity.ok(reviewService.countReviewsByTravel(travelId));
    }

    // Thêm phản hồi của chủ homestay cho đánh giá
    @PostMapping("/{id}/reply")
    public ResponseEntity<?> addOwnerReply(
            @PathVariable("id") Long id,
            @RequestBody ReviewReplyRequestDTO replyRequest,
            Authentication authentication) {

        try {
            User owner = userService.getUserByUsername(authentication.getName());
            Review updatedReview = reviewService.addOwnerReply(id, replyRequest.getReply(), owner);
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Tạo đánh giá từ booking đã hoàn thành
    @PostMapping("/booking")
    public ResponseEntity<?> createReviewFromBooking(
            @RequestBody ReviewRequestDTO reviewRequest,
            Authentication authentication) {

        try {
            User user = userService.getUserByUsername(authentication.getName());
            Review newReview = reviewService.createReviewFromBooking(
                reviewRequest.getHomestayId(),
                reviewRequest.getBookingId(),
                reviewRequest.getRating(),
                reviewRequest.getComment(),
                user
            );
            return ResponseEntity.ok(newReview);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}