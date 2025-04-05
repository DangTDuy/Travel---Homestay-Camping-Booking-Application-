package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Review;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);
    List<Review> findByServiceType(String serviceType);
    List<Review> findByServiceId(Long serviceId);
    
    @Query("SELECT DISTINCT r.homestay FROM Review r WHERE r.user = ?1 AND r.homestay IS NOT NULL")
    List<Homestay> findHomestaysByUser(User user);
}
