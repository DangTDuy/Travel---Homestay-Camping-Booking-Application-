package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Review;
import ut.edu.project.services.ReviewService;

import java.util.List;
import java.util.Optional;

@RestController // Đánh dấu đây là một REST Controller
@RequestMapping("/reviews") // Định nghĩa đường dẫn gốc cho các API liên quan đến đánh giá
public class ReviewController {

    @Autowired // Tiêm ReviewService tự động
    private ReviewService reviewService;

    @GetMapping // Lấy danh sách tất cả đánh giá
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}") // Lấy thông tin đánh giá theo ID
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Optional<Review> review = reviewService.getReviewById(id);
        return review.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping // Tạo một đánh giá mới
    public Review createReview(@RequestBody Review review) {
        return reviewService.saveReview(review);
    }

    @PutMapping("/{id}") // Cập nhật thông tin đánh giá theo ID
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review review) {
        Optional<Review> existingReview = reviewService.getReviewById(id);
        if (existingReview.isPresent()) {
            review.setId(id); // Gán ID từ đường dẫn
            Review updatedReview = reviewService.saveReview(review);
            return ResponseEntity.ok(updatedReview);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}") // Xóa đánh giá theo ID
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        Optional<Review> review = reviewService.getReviewById(id);
        if (review.isPresent()) {
            reviewService.deleteReview(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/service/{serviceType}/{serviceId}") // Lấy đánh giá theo loại dịch vụ và ID dịch vụ
    public List<Review> getReviewsByService(
            @PathVariable String serviceType,
            @PathVariable Long serviceId) {
        return reviewService.getReviewsByService(serviceType, serviceId);
    }

    @GetMapping("/user/{userId}") // Lấy đánh giá của một người dùng
    public List<Review> getReviewsByUser(@PathVariable Long userId) {
        return reviewService.getReviewsByUser(userId);
    }
}