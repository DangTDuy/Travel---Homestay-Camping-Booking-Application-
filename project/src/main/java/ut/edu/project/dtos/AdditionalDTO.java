package ut.edu.project.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalTime;
import ut.edu.project.models.TimeSlot;
import ut.edu.project.models.Category;
import ut.edu.project.models.Additional.ServiceType;

/**
 * DTO cho dịch vụ bổ sung
 * Kết hợp thông tin đầy đủ cho hiển thị và thông tin cần thiết cho đặt phòng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalDTO {
    // Thông tin cơ bản
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;

    // Quan hệ với các đối tượng khác
    private TimeSlot timeSlot;
    private Category category;
    private Long homestayId;
    private Long campingId;
    private Long travelId;

    // Thông tin thời gian
    private LocalTime startTime;
    private LocalTime endTime;

    // Trạng thái
    private boolean isActive;
    private String imageUrl;

    // Thông tin bổ sung cho đặt phòng
    private int quantity = 1;

    // Loại dịch vụ
    private ServiceType serviceType = ServiceType.ALL;

    // ID tham chiếu (dùng khi không cần đối tượng đầy đủ)
    private Long categoryId;
    private Long timeSlotId;
}