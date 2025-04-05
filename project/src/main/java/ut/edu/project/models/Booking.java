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

    @ManyToOne
    @JoinColumn(name = "homestay_id")
    private Homestay homestay;

    @Column(name = "service_type", nullable = false)
    @NotNull
    private String serviceType;

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
    private Double totalPrice;

    @Column(name = "status", nullable = false)
    @NotNull
    private String status;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Payment payment;

    // Hằng số cho serviceType và status
    public static final String SERVICE_TYPE_HOTEL = "HOTEL";
    public static final String SERVICE_TYPE_CAMPING = "CAMPING";
    public static final String SERVICE_TYPE_TRAVEL = "TRAVEL";

    public static final String STATUS_BOOKED = "BOOKED";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";
}