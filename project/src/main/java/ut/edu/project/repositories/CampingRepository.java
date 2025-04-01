package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Camping;

import java.util.List;

@Repository
public interface CampingRepository extends JpaRepository<Camping, Long> {
    List<Camping> findByIsAvailable(boolean isAvailable);
}
