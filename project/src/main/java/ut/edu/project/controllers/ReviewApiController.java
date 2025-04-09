package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.dtos.ReplyRequestDTO;
import ut.edu.project.dtos.ReviewRequestDTO;
import ut.edu.project.dtos.ReviewUpdateDTO;
import ut.edu.project.models.Review;
import ut.edu.project.models.User;
import ut.edu.project.services.ReviewService;
import ut.edu.project.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(ReviewApiController.class);

    @Autowired
    public ReviewApiController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    /**
     * Tạo đánh giá mới thông qua API
     */
    @PostMapping
    public ResponseEntity<?> createReview(
            @RequestBody ReviewRequestDTO reviewRequest,
            Authentication authentication) {
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            
            // Nếu có bookingId, sử dụng phương thức tạo đánh giá từ booking
            if (reviewRequest.getBookingId() != null) {
                Review newReview = reviewService.createReviewFromBooking(
                    reviewRequest.getHomestayId(),
                    reviewRequest.getBookingId(),
                    reviewRequest.getRating(),
                    reviewRequest.getComment(),
                    user
                );
                return ResponseEntity.ok(newReview);
            } 
            // Nếu không có bookingId, sử dụng phương thức tạo đánh giá thông thường
            else {
                Review newReview = reviewService.createReview(
                    reviewRequest.getHomestayId(),
                    user.getUsername(),
                    reviewRequest.getRating(),
                    reviewRequest.getComment()
                );
                return ResponseEntity.ok(newReview);
            }
        } catch (Exception e) {
            logger.error("Error creating review: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Thêm phản hồi của chủ homestay hoặc admin cho đánh giá
     */
    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<?> replyToReview(
            @PathVariable Long reviewId,
            @RequestBody ReplyRequestDTO replyRequest,
            Authentication authentication) {
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            Review updatedReview = reviewService.addOwnerReply(
                reviewId,
                replyRequest.getReply(),
                user
            );
            
            return ResponseEntity.ok(updatedReview);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Lấy thông tin đánh giá
     */
    @GetMapping("/{reviewId}")
public ResponseEntity<?> getReviewById(@PathVariable Long reviewId) {
    try {
        logger.info("Đang lấy thông tin đánh giá với ID: {}", reviewId);
        Review review = reviewService.getReviewById(reviewId);
        logger.info("Đã tìm thấy đánh giá: {}", review);
        return ResponseEntity.ok(review);
    } catch (Exception e) {
        logger.error("Lỗi khi lấy đánh giá với ID {}: {}", reviewId, e.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Không tìm thấy đánh giá với ID: " + reviewId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
    
    /**
     * Cập nhật đánh giá
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateDTO reviewUpdateDTO,
            Authentication authentication) {
        try {
            User user = userService.getUserByUsername(authentication.getName());
            Review updatedReview = reviewService.updateReview(
                    reviewId,
                    reviewUpdateDTO.getRating(),
                    reviewUpdateDTO.getComment(),
                    user
            );
            return ResponseEntity.ok(updatedReview);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Xóa đánh giá
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        try {
            User user = userService.getUserByUsername(authentication.getName());
            reviewService.deleteReview(reviewId, user);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đánh giá đã được xóa thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Cập nhật phản hồi
     */
    @PutMapping("/{reviewId}/reply/edit")
    public ResponseEntity<?> updateReply(
            @PathVariable Long reviewId,
            @RequestBody ReplyRequestDTO replyRequest,
            Authentication authentication) {
        try {
            User user = userService.getUserByUsername(authentication.getName());
            Review updatedReview = reviewService.updateOwnerReply(
                    reviewId,
                    replyRequest.getReply(),
                    user
            );
            return ResponseEntity.ok(updatedReview);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Xóa phản hồi
     */
    @DeleteMapping("/{reviewId}/reply")
    public ResponseEntity<?> deleteReply(
            @PathVariable Long reviewId,
            Authentication authentication) {
        try {
            User user = userService.getUserByUsername(authentication.getName());
            reviewService.deleteOwnerReply(reviewId, user);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Phản hồi đã được xóa thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Kiểm tra đánh giá tồn tại
     */
    @GetMapping("/{reviewId}/exists")
    public ResponseEntity<?> reviewExists(@PathVariable Long reviewId) {
        logger.info("Kiểm tra tồn tại đánh giá với ID: " + reviewId);
        try {
            boolean exists = reviewService.reviewExists(reviewId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("exists", false);
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }
}
