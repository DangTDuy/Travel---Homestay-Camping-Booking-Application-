package ut.edu.project.dtos;

import ut.edu.project.models.Payment;
import ut.edu.project.models.User;

public class PaymentDTO {
    private Long id;
    private String payerUsername;
    private String payerEmail;
    private Double amount;
    private String status;

    public PaymentDTO(Payment payment) {
        this.id = payment.getId();
        User payer = payment.getPayer();
        this.payerUsername = payer.getUsername();
        this.payerEmail = payer.getEmail();
        this.amount = payment.getAmount();
        this.status = payment.getStatus();
    }

    // Getters
    public Long getId() { return id; }
    public String getPayerUsername() { return payerUsername; }
    public String getPayerEmail() { return payerEmail; }
    public Double getAmount() { return amount; }
    public String getStatus() { return status; }
}
