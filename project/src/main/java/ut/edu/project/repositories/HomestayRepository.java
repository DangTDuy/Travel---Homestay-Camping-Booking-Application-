package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;

import java.util.List;

public interface HomestayRepository extends JpaRepository<Homestay, Long> {
    List<Homestay> findByOwner(User owner);
    List<Homestay> findByLocationContainingIgnoreCase(String location);
    @Query("SELECT h FROM Homestay h WHERE h.tags LIKE %:tag%")
    List<Homestay> findByTag(@Param("tag") String tag);
}