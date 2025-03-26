package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ut.edu.project.models.Camping;

public interface CampingRepository extends JpaRepository<Camping, Long> {
}