package ut.edu.project.dtos;

import java.time.LocalDate;

// Giả sử bạn dùng Lombok, nếu không, hãy tự thêm getters/setters
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {
    private Long homestayId;
    private String checkInDate; // Giữ kiểu String để binding ban đầu
    private String checkOutDate; // Giữ kiểu String để binding ban đầu
    private int guests;
    private String specialRequests;
} 