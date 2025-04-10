package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.TimeSlot;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    /**
     * Tìm time slot theo tên
     * @param name Tên time slot
     * @return TimeSlot tìm thấy hoặc null nếu không tìm thấy
     */
    TimeSlot findByName(String name);
}