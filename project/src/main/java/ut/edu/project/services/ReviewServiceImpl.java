package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.project.repositories.ReviewRepository;
import ut.edu.project.models.Review;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    @Override
    public Review saveReview(Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Số sao phải nằm trong khoảng từ 1 đến 5");
        }
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    @Override
    public List<Review> getReviewsByService(String serviceType, Long serviceId) {
        return reviewRepository.findByServiceTypeAndServiceId(serviceType, serviceId);
    }

    @Override
    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
}
