package ut.edu.project.services;

import ut.edu.project.models.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Review saveReview(Review review);
    Optional<Review> getReviewById(Long id);
    List<Review> getAllReviews();
    void deleteReview(Long id);
}
