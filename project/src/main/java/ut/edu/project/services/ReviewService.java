package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.project.models.*;
import ut.edu.project.repositories.ReviewRepository;
import ut.edu.project.repositories.CampingRepository;
import ut.edu.project.repositories.HomestayRepository;
import ut.edu.project.repositories.TravelRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private CampingRepository campingRepository;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Review createReview(Review review) {
        validateReview(review);
        Review savedReview = reviewRepository.save(review);
        updateServiceRating(review);
        return savedReview;
    }

    @Transactional
    public Review updateReview(Long id, Review review) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        Integer oldRating = existingReview.getRating();

        existingReview.setRating(review.getRating());
        existingReview.setComment(review.getComment());
        existingReview.setImages(review.getImages());

        validateReview(existingReview);
        Review updatedReview = reviewRepository.save(existingReview);

        if (!oldRating.equals(review.getRating())) {
            updateServiceRating(existingReview);
        }

        return updatedReview;
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<Review> getReviewsByUser(User user) {
        return reviewRepository.findByUser(user);
    }

    public List<Review> getReviewsByHomestay(Long homestayId) {
        return reviewRepository.findByHomestayId(homestayId);
    }

    public List<Review> getReviewsByCamping(Long campingId) {
        return reviewRepository.findByCampingId(campingId);
    }

    public List<Review> getReviewsByTravel(Long travelId) {
        return reviewRepository.findByTravelId(travelId);
    }

    public List<Review> searchReviews(
            Long userId,
            Long homestayId,
            Long campingId,
            Long travelId,
            Integer minRating,
            Integer maxRating) {
        return reviewRepository.searchReviews(
                userId, homestayId, campingId, travelId, minRating, maxRating);
    }

    public List<Review> getRecentReviews() {
        return reviewRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        reviewRepository.delete(review);
        updateServiceRating(review);
    }

    private void validateReview(Review review) {
        if (review.getUser() == null) {
            throw new RuntimeException("Người dùng không được để trống");
        }
        if (review.getBooking() == null) {
            throw new RuntimeException("Booking không được để trống");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new RuntimeException("Điểm đánh giá phải từ 1 đến 5");
        }
        if (review.getHomestay() == null && review.getCamping() == null && review.getTravel() == null) {
            throw new RuntimeException("Phải có ít nhất một loại dịch vụ được đánh giá");
        }
    }

    private void updateServiceRating(Review review) {
        if (review.getHomestay() != null) {
            Double averageRating = reviewRepository.getAverageRatingByHomestay(review.getHomestay().getId());
            Homestay homestay = review.getHomestay();
            homestay.setRating(averageRating);
            homestayRepository.save(homestay);
        }
        if (review.getCamping() != null) {
            Double averageRating = reviewRepository.getAverageRatingByCamping(review.getCamping().getId());
            Camping camping = review.getCamping();
            camping.setRating(averageRating);
            campingRepository.save(camping);
        }
        if (review.getTravel() != null) {
            Double averageRating = reviewRepository.getAverageRatingByTravel(review.getTravel().getId());
            Travel travel = review.getTravel();
            travel.setRating(averageRating);
            travelRepository.save(travel);
        }
    }

    public Double getAverageRatingByHomestay(Long homestayId) {
        return reviewRepository.getAverageRatingByHomestay(homestayId);
    }

    public Double getAverageRatingByCamping(Long campingId) {
        return reviewRepository.getAverageRatingByCamping(campingId);
    }

    public Double getAverageRatingByTravel(Long travelId) {
        return reviewRepository.getAverageRatingByTravel(travelId);
    }

    public Long countReviewsByHomestay(Long homestayId) {
        return reviewRepository.countByHomestay(homestayId);
    }

    public Long countReviewsByCamping(Long campingId) {
        return reviewRepository.countByCamping(campingId);
    }

    public Long countReviewsByTravel(Long travelId) {
        return reviewRepository.countByTravel(travelId);
    }

    public Review createReview(Long homestayId, String username, Integer rating, String comment) {
        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy homestay"));

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Review review = new Review();
        review.setHomestay(homestay);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);

        Review savedReview = reviewRepository.save(review);
        updateServiceRating(review);
        return savedReview;
    }
}
