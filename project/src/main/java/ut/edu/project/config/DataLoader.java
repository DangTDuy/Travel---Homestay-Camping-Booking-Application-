package ut.edu.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ut.edu.project.models.Travel;
import ut.edu.project.models.User;
import ut.edu.project.services.TravelService;
import ut.edu.project.services.UserService;
import ut.edu.project.dtos.RegisterDTO;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private TravelService travelService;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem đã có tour nào chưa
        if (travelService.getTotalTravelCount() == 0) {
            // Tạo hướng dẫn viên mẫu
            RegisterDTO guideDTO = new RegisterDTO();
            guideDTO.setUsername("guide1");
            guideDTO.setPassword("123456");
            guideDTO.setHoTen("Nguyễn Văn Hướng Dẫn");
            guideDTO.setEmail("guide1@example.com");
            guideDTO.setSoDienThoai("0123456789");
            guideDTO.setRole("GUIDE");
            
            // Đăng ký user guide
            userService.registerUser(guideDTO);
            
            // Đợi một chút để đảm bảo user được lưu
            Thread.sleep(1000);
            
            // Lấy user guide vừa tạo
            User guide = userService.getUserByUsername("guide1");
            
            if (guide == null) {
                throw new RuntimeException("Không thể tạo user guide");
            }

            // Tạo các tour mẫu
            List<Travel> sampleTours = Arrays.asList(
                createSampleTour(
                    "Khám phá Sapa",
                    "Sapa, Lào Cai",
                    2500000.0,
                    "Ngày 1: Khám phá thị trấn Sapa\n" +
                    "Ngày 2: Trekking bản Cát Cát\n" +
                    "Ngày 3: Thăm bản Tả Phìn",
                    3,
                    15,
                    5,
                    Arrays.asList(
                        "https://images.unsplash.com/photo-1552465011-b4e21bf6e79a",
                        "https://images.unsplash.com/photo-1552465011-b4e21bf6e79a"
                    ),
                    Arrays.asList("Ăn sáng", "Hướng dẫn viên", "Vé tham quan"),
                    "Cần mang giày thể thao, áo ấm",
                    "Ngắm ruộng bậc thang, trải nghiệm văn hóa dân tộc",
                    Travel.DifficultyLevel.NORMAL,
                    guide
                ),
                createSampleTour(
                    "Hạ Long Bay",
                    "Hạ Long, Quảng Ninh",
                    3500000.0,
                    "Ngày 1: Tham quan vịnh Hạ Long\n" +
                    "Ngày 2: Khám phá hang động\n" +
                    "Ngày 3: Tắm biển và thưởng thức hải sản",
                    3,
                    20,
                    8,
                    Arrays.asList(
                        "https://images.unsplash.com/photo-1552465011-b4e21bf6e79a",
                        "https://images.unsplash.com/photo-1552465011-b4e21bf6e79a"
                    ),
                    Arrays.asList("Ăn sáng", "Hướng dẫn viên", "Vé tham quan", "Tàu du lịch"),
                    "Mang theo đồ bơi, kem chống nắng",
                    "Ngắm cảnh vịnh, tắm biển, thưởng thức hải sản",
                    Travel.DifficultyLevel.EASY,
                    guide
                ),
                createSampleTour(
                    "Trekking Fansipan",
                    "Sapa, Lào Cai",
                    5000000.0,
                    "Ngày 1: Xuất phát từ Sapa\n" +
                    "Ngày 2: Trekking lên đỉnh Fansipan\n" +
                    "Ngày 3: Nghỉ ngơi và trở về",
                    3,
                    10,
                    4,
                    Arrays.asList(
                        "https://images.unsplash.com/photo-1552465011-b4e21bf6e79a",
                        "https://images.unsplash.com/photo-1552465011-b4e21bf6e79a"
                    ),
                    Arrays.asList("Ăn sáng", "Hướng dẫn viên", "Thiết bị leo núi"),
                    "Cần có sức khỏe tốt, mang giày leo núi",
                    "Chinh phục nóc nhà Đông Dương",
                    Travel.DifficultyLevel.CHALLENGING,
                    guide
                )
            );

            // Lưu các tour vào database
            for (Travel tour : sampleTours) {
                travelService.createTravel(tour);
            }
        }
    }

    private Travel createSampleTour(
            String tourName,
            String location,
            Double price,
            String itinerary,
            Integer durationDays,
            Integer maxParticipants,
            Integer minParticipants,
            List<String> imageUrls,
            List<String> includedServices,
            String requirements,
            String highlights,
            Travel.DifficultyLevel difficultyLevel,
            User guide) {
        Travel tour = new Travel();
        tour.setTourName(tourName);
        tour.setLocation(location);
        tour.setPrice(price);
        tour.setItinerary(itinerary);
        tour.setDurationDays(durationDays);
        tour.setMaxParticipants(maxParticipants);
        tour.setMinParticipants(minParticipants);
        tour.setImageUrls(imageUrls);
        tour.setIncludedServices(includedServices);
        tour.setRequirements(requirements);
        tour.setHighlights(highlights);
        tour.setDifficultyLevel(difficultyLevel);
        tour.setGuide(guide);
        tour.setAvailable(true);
        return tour;
    }
} 