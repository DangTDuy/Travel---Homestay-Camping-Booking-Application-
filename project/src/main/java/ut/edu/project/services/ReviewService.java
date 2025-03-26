package ut.edu.project.services;

import ut.edu.project.models.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    // Lấy tất cả đánh giá
    List<Review> getAllReviews();

    // Lấy đánh giá theo ID
    Optional<Review> getReviewById(Long id);

    // Tạo hoặc cập nhật đánh giá
    Review saveReview(Review review);

    // Xóa đánh giá theo ID
    void deleteReview(Long id);

    // Lấy đánh giá theo loại dịch vụ và ID dịch vụ
    List<Review> getReviewsByService(String serviceType, Long serviceId);

    // Lấy đánh giá của một người dùng
    List<Review> getReviewsByUser(Long userId);
}
