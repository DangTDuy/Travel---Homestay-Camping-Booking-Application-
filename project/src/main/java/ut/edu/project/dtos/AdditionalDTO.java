package ut.edu.project.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;
import ut.edu.project.models.TimeSlot;
import ut.edu.project.models.Category;

@Data
public class AdditionalDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private TimeSlot timeSlot;
    private Category category;
    private Long homestayId;
    private Long campingId;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isActive;
    private String imageUrl;
} 