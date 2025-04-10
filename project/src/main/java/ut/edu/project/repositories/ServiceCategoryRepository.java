package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ut.edu.project.models.Category;

public interface ServiceCategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
}
