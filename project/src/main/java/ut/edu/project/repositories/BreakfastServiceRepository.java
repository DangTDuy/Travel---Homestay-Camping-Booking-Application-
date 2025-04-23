package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.BreakfastService;
import ut.edu.project.models.Homestay;

import java.util.List;

@Repository
public interface BreakfastServiceRepository extends JpaRepository<BreakfastService, Long> {
    List<BreakfastService> findByHomestay(Homestay homestay);
    List<BreakfastService> findByIsAvailableTrue();
    List<BreakfastService> findByIsVegetarianTrue();
    List<BreakfastService> findByIsVeganTrue();
    List<BreakfastService> findByIsGlutenFreeTrue();
    List<BreakfastService> findByIsDairyFreeTrue();
}
