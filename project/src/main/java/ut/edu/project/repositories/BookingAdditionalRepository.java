package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.BookingAdditional;

import java.util.List;

@Repository
public interface BookingAdditionalRepository extends JpaRepository<BookingAdditional, Long> {
    List<BookingAdditional> findByBookingId(Long bookingId);
    
    @Query("SELECT ba FROM BookingAdditional ba WHERE ba.booking.id = :bookingId AND ba.additional.id = :additionalId")
    List<BookingAdditional> findByBookingIdAndAdditionalId(@Param("bookingId") Long bookingId, @Param("additionalId") Long additionalId);
}
