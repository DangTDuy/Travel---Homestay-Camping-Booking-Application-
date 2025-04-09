package ut.edu.project.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequestDTO {
    @NotNull(message = "Điểm đánh giá không được để trống")
    @Min(value = 1, message = "Điểm đánh giá phải từ 1 đến 5")
    @Max(value = 5, message = "Điểm đánh giá phải từ 1 đến 5")
    private Integer rating;
    
    @NotBlank(message = "Nội dung đánh giá không được để trống")
    private String comment;
    
    private Long homestayId;
    
    private Long campingId;
    
    private Long travelId;
    
    @NotNull(message = "ID đặt phòng không được để trống")
    private Long bookingId;
}
