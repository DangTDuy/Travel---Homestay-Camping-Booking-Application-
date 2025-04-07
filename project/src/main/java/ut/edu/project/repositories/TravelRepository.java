package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Travel;
import ut.edu.project.models.User;
import ut.edu.project.models.Travel.DifficultyLevel;

import java.util.List;

@Repository
public interface TravelRepository extends JpaRepository<Travel, Long> {
    // Tìm theo location
    List<Travel> findByLocationContainingIgnoreCase(String location);

    // Tìm theo khoảng giá
    List<Travel> findByPriceBetween(Double minPrice, Double maxPrice);

    // Tìm theo hướng dẫn viên
    List<Travel> findByGuide(User guide);

    // Tìm theo số ngày
    List<Travel> findByDurationDaysBetween(Integer minDays, Integer maxDays);

    // Tìm theo mức độ khó
    List<Travel> findByDifficultyLevel(DifficultyLevel level);

    // Tìm tour có sẵn
    List<Travel> findByIsAvailableTrue();

    // Tìm theo rating
    List<Travel> findByRatingGreaterThanEqual(Double rating);

    // Tìm theo dịch vụ bao gồm
    @Query("SELECT DISTINCT t FROM Travel t JOIN t.includedServices s WHERE s IN :services")
    List<Travel> findByIncludedServicesIn(@Param("services") List<String> services);

    // Tìm kiếm tổng hợp
    @Query("SELECT t FROM Travel t WHERE " +
           "(:location IS NULL OR LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:minPrice IS NULL OR t.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR t.price <= :maxPrice) AND " +
           "(:minDays IS NULL OR t.durationDays >= :minDays) AND " +
           "(:maxDays IS NULL OR t.durationDays <= :maxDays) AND " +
           "(:difficultyLevel IS NULL OR t.difficultyLevel = :difficultyLevel) AND " +
           "(:isAvailable IS NULL OR t.isAvailable = :isAvailable)")
    List<Travel> searchTravels(
            @Param("location") String location,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minDays") Integer minDays,
            @Param("maxDays") Integer maxDays,
            @Param("difficultyLevel") DifficultyLevel difficultyLevel,
            @Param("isAvailable") Boolean isAvailable);

    // Tìm tour có đủ số người đăng ký tối thiểu
    @Query("SELECT t FROM Travel t WHERE SIZE(t.bookings) >= t.minParticipants")
    List<Travel> findConfirmedTours();

    // Tìm tour còn chỗ
    @Query("SELECT t FROM Travel t WHERE SIZE(t.bookings) < t.maxParticipants")
    List<Travel> findAvailableTours();

    // Top rated tours
    List<Travel> findTop10ByOrderByRatingDesc();

    // Most booked tours
    List<Travel> findTop10ByOrderByBookingCountDesc();

    // Recently added tours
    List<Travel> findTop10ByOrderByCreatedAtDesc();

    // Add a method to count all travels
    long count();

    // Optional: Add a method to count available travels
    long countByIsAvailableTrue();
}
