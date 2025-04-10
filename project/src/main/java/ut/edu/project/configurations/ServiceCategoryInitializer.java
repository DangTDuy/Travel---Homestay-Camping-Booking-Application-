package ut.edu.project.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ut.edu.project.models.Category;
import ut.edu.project.repositories.CategoryRepository;

import java.util.Arrays;
import java.util.List;

@Component
@Order(3) // Run after TimeSlotInitializer
@RequiredArgsConstructor
public class ServiceCategoryInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            List<Category> defaultCategories = Arrays.asList(
                    Category.builder()
                            .name("meals")
                            .displayName("Bữa ăn")
                            .build(),
                    Category.builder()
                            .name("transportation")
                            .displayName("Phương tiện di chuyển")
                            .build(),
                    Category.builder()
                            .name("activities")
                            .displayName("Hoạt động giải trí")
                            .build(),
                    Category.builder()
                            .name("tours")
                            .displayName("Tour tham quan")
                            .build(),
                    Category.builder()
                            .name("amenities")
                            .displayName("Tiện nghi bổ sung")
                            .build()
            );

            categoryRepository.saveAll(defaultCategories);
        }
    }
}
