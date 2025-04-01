package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(name = "service_type", nullable = false)
    @NotNull
    private String serviceType; // Homestay, Camping, Travel

    @Column(name = "service_id", nullable = false)
    @NotNull
    private Long serviceId;

    @Column(name = "start_date", nullable = false)
    @NotNull
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @NotNull
    private LocalDate endDate;

    @Column(name = "total_price", nullable = false)
    @NotNull
    private Double totalPrice; // Tổng giá

    @Column(name = "status", nullable = false)
    @NotNull
    private String status; // Đã đặt, Đã thanh toán, Hủy

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Payment payment; // Liên kết với Payment
}