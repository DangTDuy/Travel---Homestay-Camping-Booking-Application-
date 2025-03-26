package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Travel;

import java.util.List;

@Repository
public interface TravelRepository extends JpaRepository<Travel, Long> {
    List<Travel> findByLocation(String location);
}
