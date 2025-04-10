package ut.edu.project.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ut.edu.project.models.Category;
import ut.edu.project.repositories.CategoryRepository;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CategoryInitializer {

    @Autowired
    private CategoryRepository categoryRepository;

    @Bean
    public CommandLineRunner initializeCategories() {
        return args -> {
            // Kiểm tra xem đã có categories chưa
            if (categoryRepository.count() == 0) {
                // Tạo các categories mặc định
                List<Category> defaultCategories = Arrays.asList(
                    createCategory("food", "Bữa ăn", "Các loại bữa ăn: sáng, trưa, tối"),
                    createCategory("drink", "Đồ uống", "Các loại đồ uống: nước ngọt, nước trái cây, rượu, bia"),
                    createCategory("service", "Dịch vụ", "Các dịch vụ bổ sung: dọn phòng, giặt ủi, đưa đón"),
                    createCategory("entertainment", "Giải trí", "Các dịch vụ giải trí: karaoke, BBQ, tour du lịch"),
                    createCategory("other", "Khác", "Các dịch vụ khác")
                );
                
                // Lưu vào database
                categoryRepository.saveAll(defaultCategories);
            }
        };
    }
    
    private Category createCategory(String name, String displayName, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDisplayName(displayName);
        category.setDescription(description);
        category.setActive(true);
        return category;
    }
}
