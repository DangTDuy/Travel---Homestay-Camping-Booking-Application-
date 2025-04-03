package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payer_id", nullable = false)
    @NotNull
    private User payer;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    @NotNull
    private Booking booking;

    @Column(nullable = false)
    @NotNull
    private Double amount;

    @Column(name = "payment_method", nullable = false)
    @NotNull
    private String paymentMethod;

    @Column(nullable = false)
    @NotNull
    private String status;

    @Column(name = "payment_date", nullable = false)
    @NotNull
    private LocalDateTime paymentDate;

    // Hằng số cho paymentMethod và status
    public static final String PAYMENT_METHOD_CREDIT_CARD = "CREDIT_CARD";
    public static final String PAYMENT_METHOD_CASH = "CASH";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
}