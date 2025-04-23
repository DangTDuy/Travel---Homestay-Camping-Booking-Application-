package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.dtos.ReviewDTO;
import ut.edu.project.models.Review;
import ut.edu.project.models.Travel;
import ut.edu.project.models.User;
import ut.edu.project.services.ReviewService;
import ut.edu.project.services.TravelService;
import ut.edu.project.services.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/travel-reviews")
public class TravelReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private TravelService travelService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewDTO reviewDTO, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Bạn cần đăng nhập để đánh giá"));
            }

            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

            Travel travel = travelService.getTravelById(reviewDTO.getTravelId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tour du lịch"));

            // Kiểm tra xem người dùng đã đặt tour này chưa
            if (!reviewService.hasUserReviewedTravel(username, reviewDTO.getTravelId())) {
                // Tạo review mới
                Review review = new Review();
                review.setUser(user);
                review.setTravel(travel);
                review.setRating(reviewDTO.getRating());
                review.setComment(reviewDTO.getComment());

                Review savedReview = reviewService.createReview(review);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Đánh giá của bạn đã được gửi thành công");
                response.put("reviewId", savedReview.getId());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Bạn đã đánh giá tour này rồi"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
