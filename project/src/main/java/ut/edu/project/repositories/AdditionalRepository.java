package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Additional;
import ut.edu.project.models.TimeSlot;
import ut.edu.project.models.Category;
import java.util.List;
import ut.edu.project.models.Homestay;

@Repository
public interface AdditionalRepository extends JpaRepository<Additional, Long> {
    List<Additional> findByHomestayId(Long homestayId);
    List<Additional> findByCampingId(Long campingId);
    List<Additional> findByTravelId(Long travelId);
    List<Additional> findByTimeSlot(TimeSlot timeSlot);
    List<Additional> findByCategory(Category category);
    List<Additional> findByIsActive(boolean isActive);
    List<Additional> findByHomestay(Homestay homestay);
    List<Additional> findByHomestayIsNull();
    // Không cần các phương thức liên quan đến booking nữa vì đã sử dụng BookingAdditional
    // List<Additional> findByBooking(Booking booking);
    // List<Additional> findByBookingId(Long bookingId);
    // @Query("SELECT a FROM Additional a WHERE a.booking.id = :bookingId")
    // List<Additional> findAllByBookingId(@Param("bookingId") Long bookingId);
}