package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Payment;
import ut.edu.project.repositories.PaymentRepository;
import ut.edu.project.services.PaymentService;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}