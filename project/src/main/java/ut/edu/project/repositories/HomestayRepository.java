package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;

import java.util.List;

public interface HomestayRepository extends JpaRepository<Homestay, Long> {
    List<Homestay> findByOwner(User owner);
    List<Homestay> findByLocationContainingIgnoreCase(String location);
}