package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.project.models.*;
import ut.edu.project.repositories.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

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

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    public Review createReview(Review review) {
        validateReview(review);
        Review savedReview = reviewRepository.save(review);
        updateServiceRating(review);
        return savedReview;
    }

    @Transactional
    public Review updateReview(Long id, Review review) {
        Optional<Review> existingReviewOpt = reviewRepository.findById(id);
        if (!existingReviewOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đánh giá");
        }
        Review existingReview = existingReviewOpt.get();

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

    public Review getReviewById(Long id) {
        Optional<Review> reviewOpt = reviewRepository.findById(id);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đánh giá với ID: " + id);
        }
        Review review = reviewOpt.get();

        // Kiểm tra các foreign key
        if (review.getBooking() != null && !bookingRepository.existsById(review.getBooking().getId())) {
            throw new RuntimeException("Booking không tồn tại cho đánh giá ID: " + id);
        }
        if (review.getHomestay() != null && !homestayRepository.existsById(review.getHomestay().getId())) {
            throw new RuntimeException("Homestay không tồn tại cho đánh giá ID: " + id);
        }

        return review;
    }

    /**
     * Kiểm tra đánh giá tồn tại
     */
    public boolean reviewExists(Long id) {
        return reviewRepository.existsById(id);
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
        Optional<Review> reviewOpt = reviewRepository.findById(id);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đánh giá");
        }
        Review review = reviewOpt.get();

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

    /**
     * Kiểm tra xem người dùng đã đánh giá tour này chưa
     * @param username Tên người dùng
     * @param travelId ID của tour
     * @return true nếu người dùng đã đánh giá tour này, false nếu chưa
     */
    public boolean hasUserReviewedTravel(String username, Long travelId) {
        try {
            User user = userService.findByUsername(username).orElse(null);
            if (user == null) {
                return false;
            }

            // Tìm travel
            Travel travel = travelRepository.findById(travelId).orElse(null);
            if (travel == null) {
                return false;
            }

            // Đếm số lượng review của user đối với travel này
            List<Review> reviews = reviewRepository.findByTravelId(travelId);

            // Kiểm tra xem có review nào của user này không
            return reviews.stream().anyMatch(review -> review.getUser() != null &&
                                               review.getUser().getId().equals(user.getId()));
        } catch (Exception e) {
            return false;
        }
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

        // Tìm booking gần nhất của user cho homestay này để liên kết với review
        // Nếu không tìm thấy, tạo một booking mới (dummy) để đáp ứng ràng buộc không null
        Booking booking = bookingRepository.findFirstByUserAndHomestayOrderByCheckOutDesc(user, homestay)
                .orElseGet(() -> {
                    Booking dummyBooking = new Booking();
                    dummyBooking.setUser(user);
                    dummyBooking.setHomestay(homestay);
                    dummyBooking.setStatus(Booking.BookingStatus.COMPLETED);
                    dummyBooking.setIsReviewed(true);

                    // Thiết lập các thuộc tính bắt buộc
                    dummyBooking.setServiceType(Booking.ServiceType.HOMESTAY);
                    dummyBooking.setCheckIn(java.time.LocalDateTime.now().minusDays(7));
                    dummyBooking.setCheckOut(java.time.LocalDateTime.now().minusDays(1));
                    dummyBooking.setGuests(1);
                    dummyBooking.setTotalPrice(0.0);

                    return bookingRepository.save(dummyBooking);
                });

        review.setBooking(booking);

        Review savedReview = reviewRepository.save(review);
        updateServiceRating(review);
        return savedReview;
    }

    /**
     * Tạo đánh giá mới từ booking
     */
    @Transactional
    public Review createReviewFromBooking(Long homestayId, Long bookingId, Integer rating, String comment, User user) {
        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy homestay"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt phòng"));

        // Kiểm tra xem booking đã hoàn thành chưa
        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Chỉ có thể đánh giá các đơn đặt phòng đã hoàn thành");
        }

        // Kiểm tra xem booking đã được đánh giá chưa
        if (booking.getIsReviewed()) {
            throw new RuntimeException("Đơn đặt phòng này đã được đánh giá");
        }

        // Kiểm tra xem người dùng có phải là người đặt phòng không
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền đánh giá đơn đặt phòng này");
        }

        Review review = new Review();
        review.setHomestay(homestay);
        review.setUser(user);
        review.setBooking(booking);
        review.setRating(rating);
        review.setComment(comment);

        // Đánh dấu booking đã được đánh giá
        booking.setIsReviewed(true);
        bookingRepository.save(booking);

        Review savedReview = reviewRepository.save(review);
        updateServiceRating(review);
        return savedReview;
    }

    /**
     * Thêm phản hồi của chủ homestay cho đánh giá
     */
    @Transactional
    public Review addOwnerReply(Long reviewId, String reply, User owner) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đánh giá với ID: " + reviewId);
        }
        Review review = reviewOpt.get();

        // Kiểm tra nếu đánh giá đã có phản hồi
        if (review.getOwnerReply() != null) {
            throw new RuntimeException("Đánh giá này đã có phản hồi");
        }

        // Kiểm tra nếu người dùng đang cố gắng trả lời đánh giá của chính mình
        if (review.getUser() != null && owner != null && review.getUser().getId().equals(owner.getId()) && !"ADMIN".equals(owner.getRole())) {
            throw new RuntimeException("Bạn không thể trả lời đánh giá của chính mình");
        }

        // Kiểm tra quyền: chỉ chủ homestay hoặc admin mới có thể trả lời
        Homestay homestay = review.getHomestay();
        if (homestay == null) {
            throw new RuntimeException("Không tìm thấy homestay của đánh giá này");
        }

        // Nếu là ADMIN, luôn cho phép trả lời
        if (owner != null && "ADMIN".equals(owner.getRole())) {
            // Admin có quyền trả lời tất cả đánh giá
        }
        // Nếu không phải ADMIN, kiểm tra xem có phải chủ homestay không
        else if (owner != null && homestay.getOwner() != null) {
            if (!homestay.getOwner().getId().equals(owner.getId())) {
                throw new RuntimeException("Bạn không có quyền trả lời đánh giá này");
            }
        } else {
            throw new RuntimeException("Bạn không có quyền trả lời đánh giá này");
        }

        // Thêm phản hồi
        review.setOwnerReply(reply);
        review.setOwnerReplyDate(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    /**
     * Cập nhật đánh giá
     */
    public Review updateReview(Long reviewId, Integer rating, String comment, User user) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đánh giá với ID: " + reviewId);
        }
        Review review = reviewOpt.get();

        // Kiểm tra quyền: chỉ người tạo đánh giá hoặc admin mới có thể sửa
        if (user == null) {
            throw new RuntimeException("Bạn không có quyền sửa đánh giá này");
        }

        // Admin có thể sửa tất cả đánh giá
        if ("ADMIN".equals(user.getRole())) {
            // Admin có quyền sửa tất cả đánh giá
        }
        // Người dùng thường chỉ có thể sửa đánh giá của chính họ
        else if (review.getUser() != null && !review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa đánh giá này");
        }

        // Cập nhật thông tin đánh giá
        review.setRating(rating);
        review.setComment(comment);

        return reviewRepository.save(review);
    }

    /**
     * Xóa đánh giá
     */
    public void deleteReview(Long reviewId, User user) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đánh giá với ID: " + reviewId);
        }
        Review review = reviewOpt.get();

        // Kiểm tra quyền: chỉ người tạo đánh giá hoặc admin mới có thể xóa
        if (user == null) {
            throw new RuntimeException("Bạn không có quyền xóa đánh giá này");
        }

        // Admin có thể xóa tất cả đánh giá
        if ("ADMIN".equals(user.getRole())) {
            // Admin có quyền xóa tất cả đánh giá
        }
        // Người dùng thường chỉ có thể xóa đánh giá của chính họ
        else if (review.getUser() != null && !review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa đánh giá này");
        }

        reviewRepository.delete(review);
    }

    /**
     * Cập nhật phản hồi của chủ homestay
     */
    public Review updateOwnerReply(Long reviewId, String reply, User user) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đánh giá với ID: " + reviewId);
        }
        Review review = reviewOpt.get();

        // Kiểm tra xem đánh giá đã có phản hồi chưa
        if (review.getOwnerReply() == null) {
            throw new RuntimeException("Đánh giá này chưa có phản hồi để cập nhật");
        }

        // Kiểm tra quyền: chỉ chủ homestay hoặc admin mới có thể sửa phản hồi
        if (!hasPermissionToEditOwnerReply(review, user)) {
            throw new RuntimeException("Bạn không có quyền sửa phản hồi này");
        }

        // Cập nhật phản hồi
        review.setOwnerReply(reply);
        review.setOwnerReplyDate(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    /**
     * Xóa phản hồi của chủ homestay
     */
    public void deleteOwnerReply(Long reviewId, User user) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đánh giá với ID: " + reviewId);
        }
        Review review = reviewOpt.get();

        // Kiểm tra xem đánh giá đã có phản hồi chưa
        if (review.getOwnerReply() == null) {
            throw new RuntimeException("Đánh giá này chưa có phản hồi để xóa");
        }

        // Kiểm tra quyền: chỉ chủ homestay hoặc admin mới có thể xóa phản hồi
        if (!hasPermissionToEditOwnerReply(review, user)) {
            throw new RuntimeException("Bạn không có quyền xóa phản hồi này");
        }

        // Xóa phản hồi
        review.setOwnerReply(null);
        review.setOwnerReplyDate(null);

        reviewRepository.save(review);
    }

    private boolean hasPermissionToEditReview(Review review, User user) {
        if (review.getUser() != null && user != null) {
            return review.getUser().getId().equals(user.getId()) || "ADMIN".equals(user.getRole());
        } else {
            return user != null && "ADMIN".equals(user.getRole());
        }
    }

    private boolean hasPermissionToEditOwnerReply(Review review, User user) {
        Homestay homestay = review.getHomestay();
        if (homestay == null) {
            throw new RuntimeException("Không tìm thấy homestay của đánh giá này");
        }

        if (user != null && homestay.getOwner() != null) {
            return homestay.getOwner().getId().equals(user.getId()) || "ADMIN".equals(user.getRole());
        } else {
            return user != null && "ADMIN".equals(user.getRole());
        }
    }
}
