package ut.edu.project.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import ut.edu.project.models.Booking; // Import Booking để lấy Enum Status
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class BookingResponseDTO {

    private Long bookingId;
    private String homestayName;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private int guests;
    private double totalPrice;
    private Booking.BookingStatus status; // Sử dụng kiểu Enum
    private String message; // Thêm thông điệp thành công tùy chọn

    // Constructor để dễ dàng tạo DTO từ Entity Booking
    public BookingResponseDTO(Booking booking, String message) {
        this.bookingId = booking.getId();
        // Kiểm tra null trước khi truy cập homestay
        if (booking.getHomestay() != null) {
            this.homestayName = booking.getHomestay().getName();
        } else {
            this.homestayName = "N/A"; // Hoặc giá trị mặc định khác
        }
        this.checkIn = booking.getCheckIn();
        this.checkOut = booking.getCheckOut();
        this.guests = booking.getGuests();
        this.totalPrice = booking.getTotalPrice();
        this.status = booking.getStatus();
        this.message = message;
    }

    // Constructor đơn giản hơn nếu không cần message
    public BookingResponseDTO(Booking booking) {
        this(booking, "Đặt phòng thành công!"); // Default success message
    }
} 