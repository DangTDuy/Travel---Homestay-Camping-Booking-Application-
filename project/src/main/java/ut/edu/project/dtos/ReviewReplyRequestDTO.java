package ut.edu.project.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewReplyRequestDTO {
    @NotBlank(message = "Nội dung phản hồi không được để trống")
    private String reply;
}
