package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ut.edu.project.models.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Tìm tất cả đánh giá theo loại dịch vụ và ID dịch vụ
    List<Review> findByServiceTypeAndServiceId(String serviceType, Long serviceId);

    // Tìm tất cả đánh giá của một người dùng
    List<Review> findByUserId(Long userId);
}