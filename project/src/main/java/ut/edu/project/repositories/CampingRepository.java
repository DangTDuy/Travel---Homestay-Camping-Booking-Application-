package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ut.edu.project.models.Camping;

import java.util.List;

public interface CampingRepository extends JpaRepository<Camping, Long> {
    List<Camping> findByIsAvailableTrue();
}
