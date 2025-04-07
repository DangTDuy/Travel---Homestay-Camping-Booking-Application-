package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Review;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);
    
    @Query("SELECT r FROM Review r WHERE " +
           "(:serviceType = 'HOMESTAY' AND r.homestay IS NOT NULL) OR " +
           "(:serviceType = 'CAMPING' AND r.camping IS NOT NULL) OR " +
           "(:serviceType = 'TRAVEL' AND r.travel IS NOT NULL)")
    List<Review> findByServiceType(@Param("serviceType") String serviceType);
    
    @Query("SELECT DISTINCT r.homestay FROM Review r WHERE r.user = ?1 AND r.homestay IS NOT NULL")
    List<Homestay> findHomestaysByUser(User user);

    // Tìm theo người dùng
    List<Review> findByUser(User user);

    // Tìm theo homestay
    List<Review> findByHomestayId(Long homestayId);

    // Tìm theo camping
    List<Review> findByCampingId(Long campingId);

    // Tìm theo travel
    List<Review> findByTravelId(Long travelId);

    // Tìm theo booking
    List<Review> findByBookingId(Long bookingId);

    // Tìm theo khoảng điểm
    List<Review> findByRatingBetween(Integer minRating, Integer maxRating);

    // Tìm theo từ khóa trong comment
    List<Review> findByCommentContainingIgnoreCase(String keyword);

    // Tìm kiếm tổng hợp
    @Query("SELECT r FROM Review r WHERE " +
           "(:userId IS NULL OR r.user.id = :userId) AND " +
           "(:homestayId IS NULL OR r.homestay.id = :homestayId) AND " +
           "(:campingId IS NULL OR r.camping.id = :campingId) AND " +
           "(:travelId IS NULL OR r.travel.id = :travelId) AND " +
           "(:minRating IS NULL OR r.rating >= :minRating) AND " +
           "(:maxRating IS NULL OR r.rating <= :maxRating)")
    List<Review> searchReviews(
            @Param("userId") Long userId,
            @Param("homestayId") Long homestayId,
            @Param("campingId") Long campingId,
            @Param("travelId") Long travelId,
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating);

    // Thống kê điểm trung bình theo homestay
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.homestay.id = :homestayId")
    Double getAverageRatingByHomestay(@Param("homestayId") Long homestayId);

    // Thống kê điểm trung bình theo camping
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.camping.id = :campingId")
    Double getAverageRatingByCamping(@Param("campingId") Long campingId);

    // Thống kê điểm trung bình theo travel
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.travel.id = :travelId")
    Double getAverageRatingByTravel(@Param("travelId") Long travelId);

    // Đếm số review theo homestay
    @Query("SELECT COUNT(r) FROM Review r WHERE r.homestay.id = :homestayId")
    Long countByHomestay(@Param("homestayId") Long homestayId);

    // Đếm số review theo camping
    @Query("SELECT COUNT(r) FROM Review r WHERE r.camping.id = :campingId")
    Long countByCamping(@Param("campingId") Long campingId);

    // Đếm số review theo travel
    @Query("SELECT COUNT(r) FROM Review r WHERE r.travel.id = :travelId")
    Long countByTravel(@Param("travelId") Long travelId);

    // Lấy review mới nhất
    List<Review> findTop10ByOrderByCreatedAtDesc();

    List<Review> findByHomestay(Homestay homestay);
    List<Review> findByHomestayOrderByCreatedAtDesc(Homestay homestay);
    Double getAverageRatingByHomestay(Homestay homestay);

    void deleteByHomestay(Homestay homestay);
}
