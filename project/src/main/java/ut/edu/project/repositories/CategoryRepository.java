package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /**
     * Tìm category theo tên
     * @param name Tên category
     * @return Category tìm thấy hoặc null nếu không tìm thấy
     */
    Category findByName(String name);
}
