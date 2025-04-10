package ut.edu.project.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ut.edu.project.models.TimeSlot;
import ut.edu.project.repositories.TimeSlotRepository;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Configuration
public class TimeSlotInitializer {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Bean
    public CommandLineRunner initializeTimeSlots() {
        return args -> {
            // Kiểm tra xem đã có time slots chưa
            if (timeSlotRepository.count() == 0) {
                // Tạo các time slots mặc định
                List<TimeSlot> defaultTimeSlots = Arrays.asList(
                    // Thêm khung giờ "Cả ngày" để sử dụng cho các dịch vụ cả ngày
                    createTimeSlot("all", "Cả ngày", "Dịch vụ có sẵn cả ngày",
                        LocalTime.of(0, 0), LocalTime.of(23, 59)),
                    createTimeSlot("morning", "Buổi sáng (6:00 - 11:00)", "Khung giờ buổi sáng từ 6:00 đến 11:00",
                        LocalTime.of(6, 0), LocalTime.of(11, 0)),
                    createTimeSlot("noon", "Buổi trưa (11:00 - 14:00)", "Khung giờ buổi trưa từ 11:00 đến 14:00",
                        LocalTime.of(11, 0), LocalTime.of(14, 0)),
                    createTimeSlot("afternoon", "Buổi chiều (14:00 - 18:00)", "Khung giờ buổi chiều từ 14:00 đến 18:00",
                        LocalTime.of(14, 0), LocalTime.of(18, 0)),
                    createTimeSlot("evening", "Buổi tối (18:00 - 22:00)", "Khung giờ buổi tối từ 18:00 đến 22:00",
                        LocalTime.of(18, 0), LocalTime.of(22, 0)),
                    createTimeSlot("night", "Đêm khuya (22:00 - 6:00)", "Khung giờ đêm khuya từ 22:00 đến 6:00",
                        LocalTime.of(22, 0), LocalTime.of(6, 0))
                );

                // Lưu vào database
                timeSlotRepository.saveAll(defaultTimeSlots);
            }
        };
    }

    private TimeSlot createTimeSlot(String name, String displayName, String description, LocalTime startTime, LocalTime endTime) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setName(name);
        timeSlot.setDisplayName(displayName);
        timeSlot.setDescription(description);
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);
        timeSlot.setActive(true);
        return timeSlot;
    }
}
