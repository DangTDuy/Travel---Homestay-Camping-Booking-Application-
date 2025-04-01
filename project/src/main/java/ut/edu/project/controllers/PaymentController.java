package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Payment;
import ut.edu.project.services.PaymentService;
import ut.edu.project.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        Payment savedPayment = paymentService.savePayment(payment);
        return ResponseEntity.ok(savedPayment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return ResponseEntity.ok(payment);
    }


    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}