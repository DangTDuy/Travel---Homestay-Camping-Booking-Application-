package ut.edu.project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ut.edu.project.models.Camping;

import java.util.List;

public interface CampingRepository extends JpaRepository<Camping, Long> {
    List<Camping> findByIsAvailableTrue();
    Page<Camping> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Camping> findByMaxPlacesGreaterThanEqual(int places, Pageable pageable);
    Page<Camping> findByIsAvailable(boolean isAvailable, Pageable pageable);
}