package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Review;
import ut.edu.project.repositories.ReviewRepository;

import java.util.List;
import java.util.Optional;

@Service // Đánh dấu đây là một lớp Service trong Spring
public class ReviewServiceImpl implements ReviewService {

    @Autowired // Tiêm ReviewRepository tự động
    private ReviewRepository reviewRepository;

    @Override
    public List<Review> getAllReviews() { // Lấy tất cả đánh giá
        return reviewRepository.findAll();
    }

    @Override
    public Optional<Review> getReviewById(Long id) { // Lấy đánh giá theo ID
        return reviewRepository.findById(id);
    }

    @Override
    public Review saveReview(Review review) { // Lưu đánh giá (tạo mới hoặc cập nhật)
        // Kiểm tra giá trị rating hợp lệ (1-5)
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Số sao phải nằm trong khoảng từ 1 đến 5");
        }
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long id) { // Xóa đánh giá theo ID
        reviewRepository.deleteById(id);
    }

    @Override
    public List<Review> getReviewsByService(String serviceType, Long serviceId) { // Lấy đánh giá theo loại dịch vụ và ID
        return reviewRepository.findByServiceTypeAndServiceId(serviceType, serviceId);
    }

    @Override
    public List<Review> getReviewsByUser(Long userId) { // Lấy đánh giá của một người dùng
        return reviewRepository.findByUserId(userId);
    }
}