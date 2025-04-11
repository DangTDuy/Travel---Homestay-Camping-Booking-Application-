package ut.edu.project.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import ut.edu.project.models.Additional;
import ut.edu.project.models.Category;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.TimeSlot;
import ut.edu.project.repositories.AdditionalRepository;
import ut.edu.project.repositories.CategoryRepository;
import ut.edu.project.repositories.HomestayRepository;
import ut.edu.project.repositories.TimeSlotRepository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Configuration
public class AdditionalServiceInitializer {

    @Autowired
    private AdditionalRepository additionalRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @Bean
    @DependsOn({"initializeTimeSlots", "initializeCategories"})
    public CommandLineRunner initializeAdditionalServices() {
        return args -> {
            // Kiểm tra xem đã có dịch vụ bổ sung chưa
            if (additionalRepository.count() > 0) {
                // Đã có dịch vụ bổ sung, nhưng vẫn tạo dịch vụ chung nếu chưa có
                if (additionalRepository.findByHomestayIsNull().isEmpty()) {
                    createCommonServices();
                }
                return;
            }

            // Lấy danh sách homestay
            List<Homestay> homestays = homestayRepository.findAll();

            // Tạo các dịch vụ chung (không gắn với homestay cụ thể)
            createCommonServices();

            // Lấy các danh mục
            Category foodCategory = categoryRepository.findByName("food");
            Category drinkCategory = categoryRepository.findByName("drink");
            Category serviceCategory = categoryRepository.findByName("service");
            Category entertainmentCategory = categoryRepository.findByName("entertainment");

            // Lấy các khung giờ
            TimeSlot morningSlot = timeSlotRepository.findByName("morning");
            TimeSlot noonSlot = timeSlotRepository.findByName("noon");
            TimeSlot afternoonSlot = timeSlotRepository.findByName("afternoon");
            TimeSlot eveningSlot = timeSlotRepository.findByName("evening");

            // Tạo dịch vụ bổ sung cho mỗi homestay
            for (Homestay homestay : homestays) {
                // Bữa sáng
                createAdditionalService(
                    "Bữa sáng Việt Nam",
                    "Bữa sáng truyền thống Việt Nam với phở, bánh mì, cà phê sữa đá",
                    new BigDecimal("150000"),
                    morningSlot,
                    foodCategory,
                    homestay,
                    LocalTime.of(6, 0),
                    LocalTime.of(10, 0)
                );

                // Bữa trưa
                createAdditionalService(
                    "Bữa trưa đặc sản",
                    "Bữa trưa với các món đặc sản địa phương",
                    new BigDecimal("200000"),
                    noonSlot,
                    foodCategory,
                    homestay,
                    LocalTime.of(11, 0),
                    LocalTime.of(14, 0)
                );

                // Bữa tối
                createAdditionalService(
                    "Bữa tối BBQ",
                    "Bữa tối BBQ ngoài trời với thịt và hải sản",
                    new BigDecimal("250000"),
                    eveningSlot,
                    foodCategory,
                    homestay,
                    LocalTime.of(18, 0),
                    LocalTime.of(21, 0)
                );

                // Đồ uống
                createAdditionalService(
                    "Gói đồ uống không giới hạn",
                    "Nước ngọt, nước trái cây, bia địa phương không giới hạn",
                    new BigDecimal("100000"),
                    timeSlotRepository.findByName("all"), // Cả ngày
                    drinkCategory,
                    homestay,
                    LocalTime.of(6, 0),
                    LocalTime.of(22, 0)
                );

                // Dịch vụ buổi sáng
                createAdditionalService(
                    "Yoga buổi sáng",
                    "Lớp yoga 1 giờ vào buổi sáng",
                    new BigDecimal("80000"),
                    morningSlot,
                    serviceCategory,
                    homestay,
                    LocalTime.of(6, 0),
                    LocalTime.of(8, 0)
                );

                // Dịch vụ buổi chiều
                createAdditionalService(
                    "Tour làng nghề",
                    "Tour tham quan làng nghề địa phương",
                    new BigDecimal("120000"),
                    afternoonSlot,
                    serviceCategory,
                    homestay,
                    LocalTime.of(14, 0),
                    LocalTime.of(17, 0)
                );

                // Dịch vụ buổi tối
                createAdditionalService(
                    "Karaoke",
                    "Phòng karaoke riêng với đồ uống miễn phí",
                    new BigDecimal("150000"),
                    eveningSlot,
                    entertainmentCategory,
                    homestay,
                    LocalTime.of(19, 0),
                    LocalTime.of(22, 0)
                );

                // Dịch vụ cả ngày
                createAdditionalService(
                    "Thuê xe máy",
                    "Thuê xe máy để khám phá khu vực",
                    new BigDecimal("100000"),
                    timeSlotRepository.findByName("all"), // Cả ngày
                    serviceCategory,
                    homestay,
                    LocalTime.of(6, 0),
                    LocalTime.of(22, 0)
                );
            }
        };
    }

    /**
     * Tạo các dịch vụ chung không gắn với homestay cụ thể
     */
    private void createCommonServices() {
        // Lấy các danh mục
        Category foodCategory = categoryRepository.findByName("food");
        Category drinkCategory = categoryRepository.findByName("drink");
        Category serviceCategory = categoryRepository.findByName("service");
        Category entertainmentCategory = categoryRepository.findByName("entertainment");

        // Lấy các khung giờ
        TimeSlot allDaySlot = timeSlotRepository.findByName("all");
        TimeSlot morningSlot = timeSlotRepository.findByName("morning");
        TimeSlot noonSlot = timeSlotRepository.findByName("noon");
        TimeSlot afternoonSlot = timeSlotRepository.findByName("afternoon");
        TimeSlot eveningSlot = timeSlotRepository.findByName("evening");

        // Dịch vụ đồ ăn chung
        createAdditionalService(
            "Bữa sáng quốc tế",
            "Bữa sáng phong cách quốc tế với trứng, thịt xông khói, salad, bánh mì",
            new BigDecimal("120000"),
            morningSlot,
            foodCategory,
            null,
            LocalTime.of(6, 0),
            LocalTime.of(10, 0)
        );

        createAdditionalService(
            "Bữa trưa set menu",
            "Bữa trưa với các món ăn được chế biến từ nguyên liệu tươi sạch",
            new BigDecimal("150000"),
            noonSlot,
            foodCategory,
            null,
            LocalTime.of(11, 0),
            LocalTime.of(14, 0)
        );

        // Dịch vụ đồ uống chung
        createAdditionalService(
            "Nước uống không giới hạn",
            "Nước lọc, nước ngọt, trà đá, cà phê không giới hạn cả ngày",
            new BigDecimal("80000"),
            allDaySlot,
            drinkCategory,
            null,
            LocalTime.of(6, 0),
            LocalTime.of(22, 0)
        );

        // Dịch vụ giải trí chung
        createAdditionalService(
            "Tour tham quan thành phố buổi sáng",
            "Tour tham quan các điểm du lịch nổi tiếng trong thành phố (4 giờ)",
            new BigDecimal("300000"),
            morningSlot,
            entertainmentCategory,
            null,
            LocalTime.of(8, 0),
            LocalTime.of(12, 0)
        );

        createAdditionalService(
            "Tour tham quan thành phố buổi chiều",
            "Tour tham quan các điểm du lịch nổi tiếng trong thành phố (4 giờ)",
            new BigDecimal("300000"),
            afternoonSlot,
            entertainmentCategory,
            null,
            LocalTime.of(14, 0),
            LocalTime.of(18, 0)
        );

        createAdditionalService(
            "Tour đêm thành phố",
            "Khám phá thành phố về đêm với các điểm tham quan đẹp nhất",
            new BigDecimal("350000"),
            eveningSlot,
            entertainmentCategory,
            null,
            LocalTime.of(19, 0),
            LocalTime.of(22, 0)
        );

        // Dịch vụ tiện ích chung
        createAdditionalService(
            "Dịch vụ giặt ủi",
            "Dịch vụ giặt ủi quần áo trong ngày",
            new BigDecimal("100000"),
            allDaySlot,
            serviceCategory,
            null,
            LocalTime.of(8, 0),
            LocalTime.of(17, 0)
        );
    }

    private void createAdditionalService(
            String name,
            String description,
            BigDecimal price,
            TimeSlot timeSlot,
            Category category,
            Homestay homestay,
            LocalTime startTime,
            LocalTime endTime) {

        Additional additional = new Additional();
        additional.setName(name);
        additional.setDescription(description);
        additional.setPrice(price);
        additional.setTimeSlot(timeSlot);
        additional.setCategory(category);
        additional.setHomestay(homestay);
        additional.setStartTime(startTime);
        additional.setEndTime(endTime);
        additional.setActive(true);
        additional.setQuantity(1);

        additionalRepository.save(additional);
    }
}
