package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Booking;
import ut.edu.project.models.Booking.BookingStatus;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Tìm booking theo user
    List<Booking> findByUserId(Long userId);
    List<Booking> findByUserUsernameOrderByCreatedAtDesc(String username);
    List<Booking> findTop3ByUserUsernameOrderByCreatedAtDesc(String username);

    // Tìm booking theo trạng thái
    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);

    // Tìm homestay đã đặt của user
    @Query("SELECT DISTINCT b.homestay FROM Booking b WHERE b.user = :user AND b.homestay IS NOT NULL")
    List<Homestay> findHomestaysByUser(@Param("user") User user);

    // Tìm kiếm booking
    @Query("SELECT b FROM Booking b WHERE (" +
            "LOWER(b.user.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.homestay.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.camping.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.travel.tourName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))" +
            ") ORDER BY b.createdAt DESC")
    List<Booking> findByUserUsernameContainingIgnoreCaseOrHomestayNameContainingIgnoreCaseOrCampingNameContainingIgnoreCaseOrTravelTourNameContainingIgnoreCaseOrderByCreatedAtDesc(
            @Param("searchTerm") String searchTerm);

    // Kiểm tra booking trùng lặp
    @Query("SELECT b FROM Booking b WHERE b.homestay.id = :homestayId " +
            "AND b.status NOT IN ('CANCELLED', 'REFUNDED') " +
            "AND (:checkIn < b.checkOut) AND (:checkOut > b.checkIn)")
    List<Booking> findOverlappingBookings(
            @Param("homestayId") Long homestayId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut);

    // Thống kê booking theo trạng thái
    @Query("SELECT b.status, COUNT(b) FROM Booking b GROUP BY b.status")
    List<Object[]> countBookingsByStatus();

    // Thống kê doanh thu theo homestay
    @Query("SELECT b.homestay.id, b.homestay.name, COUNT(b), SUM(b.totalPrice) " +
            "FROM Booking b WHERE b.status = 'COMPLETED' " +
            "GROUP BY b.homestay.id, b.homestay.name ORDER BY SUM(b.totalPrice) DESC")
    List<Object[]> getRevenueByHomestay();

    // Lấy booking gần đây
    List<Booking> findTop10ByOrderByCreatedAtDesc();

    // Ghi đè phương thức findAll để sắp xếp mặc định
    @Override
    List<Booking> findAll();

    // Định nghĩa phương thức findAll có sắp xếp
    List<Booking> findAllByOrderByCreatedAtDesc();

    void deleteByHomestay(Homestay homestay);

    // Đếm số lượng booking đã hoàn thành của user đối với một homestay cụ thể
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.homestay.id = :homestayId AND b.status = :status")
    Long countByUserAndHomestayAndStatus(
            @Param("userId") Long userId,
            @Param("homestayId") Long homestayId,
            @Param("status") String status);
}