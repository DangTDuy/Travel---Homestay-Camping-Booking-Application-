package ut.edu.project.services;

import ut.edu.project.models.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking saveBooking(Booking booking, String paymentMethod); // Thêm paymentMethod để tạo Payment
    Optional<Booking> getBookingById(Long id);
    List<Booking> getAllBookings();
    void deleteBooking(Long id);
    Booking updateBookingStatus(Long id, String status); // Cập nhật trạng thái
    List<Booking> getBookingsByUser(Long userId); // Lấy booking theo user
}