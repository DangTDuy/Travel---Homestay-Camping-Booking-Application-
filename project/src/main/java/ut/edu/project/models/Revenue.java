package ut.edu.project.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "revenues")
public class Revenue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "admin_fee", nullable = false)
    private BigDecimal adminFee;

    @Column(name = "owner_amount", nullable = false)
    private BigDecimal ownerAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private RevenueStatus status;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 