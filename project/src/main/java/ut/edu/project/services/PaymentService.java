package ut.edu.project.services;

import ut.edu.project.models.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Payment savePayment(Payment payment);
    Optional<Payment> getPaymentById(Long id);
    List<Payment> getAllPayments();
    void deletePayment(Long id);
    Optional<Payment> getPaymentByBookingId(Long bookingId); // Lấy payment theo booking
}