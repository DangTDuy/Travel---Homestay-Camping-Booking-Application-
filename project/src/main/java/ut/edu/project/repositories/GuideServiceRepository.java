package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.GuideService;
import ut.edu.project.models.Travel;

import java.util.List;

@Repository
public interface GuideServiceRepository extends JpaRepository<GuideService, Long> {
    List<GuideService> findByTravel(Travel travel);
    List<GuideService> findByIsAvailableTrue();
    List<GuideService> findByLanguage(String language);
    List<GuideService> findByExperienceYearsGreaterThanEqual(Integer years);
    
    @Query("SELECT g FROM GuideService g WHERE :specialty MEMBER OF g.specialties")
    List<GuideService> findBySpecialty(@Param("specialty") String specialty);
}
