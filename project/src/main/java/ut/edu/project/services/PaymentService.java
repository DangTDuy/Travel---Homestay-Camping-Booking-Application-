package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Payment;
import ut.edu.project.repositories.PaymentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    public Optional<Payment> getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public List<Payment> getPaymentsByPayerId(Long payerId) {
        return paymentRepository.findByPayerId(payerId);
    }

    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    // Additional methods based on requirements can be added here.
}