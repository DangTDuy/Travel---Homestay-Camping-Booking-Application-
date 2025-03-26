package ut.edu.project.services;

import ut.edu.project.models.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking saveBooking(Booking booking);
    Optional<Booking> getBookingById(Long id);
    List<Booking> getAllBookings();
    void deleteBooking(Long id);
}
