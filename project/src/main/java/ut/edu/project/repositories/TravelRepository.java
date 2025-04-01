package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ut.edu.project.models.Travel;

public interface TravelRepository extends JpaRepository<Travel, Long> {
}
