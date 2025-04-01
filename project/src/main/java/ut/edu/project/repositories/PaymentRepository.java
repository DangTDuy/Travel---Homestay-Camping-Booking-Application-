package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Payment;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPayerId(Long payerId);
    List<Payment> findByStatus(String status);
    Optional<Payment> findByBookingId(Long bookingId); // Tìm payment theo booking
}