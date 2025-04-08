package ut.edu.project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Camping;
import ut.edu.project.models.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampingRepository extends JpaRepository<Camping, Long> {
    // Tìm khu cắm trại theo địa điểm (không phân biệt chữ hoa/thường)
    List<Camping> findByLocationContainingIgnoreCase(String location);

    // Tìm khu cắm trại theo khoảng giá
    List<Camping> findByPriceBetween(Double minPrice, Double maxPrice);

    // Tìm khu cắm trại có số chỗ lớn hơn hoặc bằng
    List<Camping> findByMaxPlacesGreaterThanEqual(Integer places);

    // Tìm khu cắm trại theo chủ sở hữu
    List<Camping> findByOwner(User owner);

    // Tìm các khu cắm trại đang sẵn sàng
    List<Camping> findByIsAvailableTrue();

    // Tìm khu cắm trại theo điểm đánh giá tối thiểu
    List<Camping> findByRatingGreaterThanEqual(Double rating);

    // Tìm khu cắm trại theo danh sách tiện ích
    @Query("SELECT DISTINCT c FROM Camping c JOIN c.facilities f WHERE f IN :facilities")
    List<Camping> findByFacilitiesIn(@Param("facilities") List<String> facilities);

    // Tìm kiếm tổng hợp với nhiều tiêu chí
    @Query("SELECT c FROM Camping c WHERE " +
            "(:location IS NULL OR LOWER(c.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:minPrice IS NULL OR c.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR c.price <= :maxPrice) AND " +
            "(:minPlaces IS NULL OR c.maxPlaces >= :minPlaces) AND " +
            "(:isAvailable IS NULL OR c.isAvailable = :isAvailable)")
    List<Camping> searchCampings(
            @Param("location") String location,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minPlaces") Integer minPlaces,
            @Param("isAvailable") Boolean isAvailable);

    // Lấy top 10 khu cắm trại theo điểm đánh giá
    List<Camping> findTop10ByOrderByRatingDesc();

    // Lấy top 10 khu cắm trại theo số lượng đặt chỗ
    List<Camping> findTop10ByOrderByBookingCountDesc();

    // Lấy top 10 khu cắm trại mới nhất
    List<Camping> findTop10ByOrderByCreatedAtDesc();

    // Tìm khu cắm trại theo tên với phân trang
    Page<Camping> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Tìm khu cắm trại theo số chỗ tối thiểu với phân trang
    Page<Camping> findByMaxPlacesGreaterThanEqual(int places, Pageable pageable);

    // Tìm khu cắm trại theo trạng thái sẵn sàng với phân trang
    Page<Camping> findByIsAvailable(boolean isAvailable, Pageable pageable);

    // Các truy vấn mới cho đánh giá
    // Tìm khu cắm trại có số lượng đánh giá tối thiểu
    @Query("SELECT c FROM Camping c WHERE SIZE(c.reviews) > :minReviews")
    List<Camping> findByMinimumReviews(@Param("minReviews") int minReviews);

    // Tìm khu cắm trại có điểm đánh giá cụ thể
    @Query("SELECT c FROM Camping c WHERE :rating MEMBER OF c.reviewRatings")
    List<Camping> findBySpecificRating(@Param("rating") Integer rating);

    // Các truy vấn mới cho thông báo
    // Tìm khu cắm trại theo khoảng thời gian thông báo
    @Query("SELECT c FROM Camping c WHERE c.lastNotificationSent >= :startDate AND c.lastNotificationSent <= :endDate")
    List<Camping> findByNotificationDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Tìm khu cắm trại theo nội dung thông báo
    @Query("SELECT c FROM Camping c WHERE :notification MEMBER OF c.notifications")
    List<Camping> findByNotificationContent(@Param("notification") String notification);

    // Các truy vấn mới cho gợi ý
    // Tìm khu cắm trại theo từ khóa tìm kiếm
    @Query("SELECT c FROM Camping c WHERE :term MEMBER OF c.searchHistory")
    List<Camping> findBySearchTerm(@Param("term") String term);

    // Tìm khu cắm trại có số lượng tìm kiếm tối thiểu
    @Query("SELECT c FROM Camping c WHERE SIZE(c.searchHistory) > :minSearches")
    List<Camping> findBySearchActivity(@Param("minSearches") int minSearches);

    // Các truy vấn mới cho thanh toán
    // Tìm khu cắm trại theo trạng thái thanh toán
    @Query("SELECT c FROM Camping c WHERE c.lastPaymentStatus = :status")
    List<Camping> findByPaymentStatus(@Param("status") String status);

    // Truy vấn phức hợp nâng cao
    @Query("SELECT c FROM Camping c WHERE " +
            "(:location IS NULL OR LOWER(c.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:minRating IS NULL OR c.rating >= :minRating) AND " +
            "(:minReviews IS NULL OR SIZE(c.reviews) >= :minReviews) AND " +
            "(:facility IS NULL OR :facility MEMBER OF c.facilities)")
    List<Camping> advancedSearch(
            @Param("location") String location,
            @Param("minRating") Double minRating,
            @Param("minReviews") Integer minReviews,
            @Param("facility") String facility);
}