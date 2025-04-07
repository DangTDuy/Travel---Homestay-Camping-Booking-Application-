package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Additional;
import ut.edu.project.models.TimeSlot;
import ut.edu.project.models.Category;
import java.util.List;
import ut.edu.project.models.Homestay;

@Repository
public interface AdditionalRepository extends JpaRepository<Additional, Long> {
    List<Additional> findByHomestayId(Long homestayId);
    List<Additional> findByCampingId(Long campingId);
    List<Additional> findByTimeSlot(TimeSlot timeSlot);
    List<Additional> findByCategory(Category category);
    List<Additional> findByIsActive(boolean isActive);
    List<Additional> findByHomestay(Homestay homestay);
} 