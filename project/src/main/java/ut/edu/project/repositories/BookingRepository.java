package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Các phương thức truy vấn tùy chỉnh nếu cần
}