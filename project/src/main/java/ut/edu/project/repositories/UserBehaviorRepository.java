package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.UserBehavior;
import ut.edu.project.models.Homestay;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {
    void deleteByHomestay(Homestay homestay);
} 