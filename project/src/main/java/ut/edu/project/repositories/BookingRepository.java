package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Booking;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId); // Tìm booking theo user
    List<Booking> findByStatus(String status); // Tìm booking theo trạng thái
    
    @Query("SELECT DISTINCT b.homestay FROM Booking b WHERE b.user = ?1 AND b.homestay IS NOT NULL")
    List<Homestay> findHomestaysByUser(User user);
}