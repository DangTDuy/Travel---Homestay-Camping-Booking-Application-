package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @NotNull(message = "Người dùng không được để trống")
    private User user;

    @ManyToOne
    @JoinColumn(name = "homestay_id")
    private Homestay homestay;

    @ManyToOne
    @JoinColumn(name = "camping_id")
    private Camping camping;

    @ManyToOne
    @JoinColumn(name = "travel_id")
    private Travel travel;

    @Column(name = "service_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Loại dịch vụ không được để trống")
    private ServiceType serviceType;

    @Column(name = "check_in", nullable = false)
    @NotNull(message = "Ngày check-in không được để trống")
    private LocalDateTime checkIn;

    @Column(name = "check_out", nullable = false)
    @NotNull(message = "Ngày check-out không được để trống")
    private LocalDateTime checkOut;

    @Column(name = "guests", nullable = false)
    @NotNull(message = "Số lượng khách không được để trống")
    @Min(value = 1, message = "Số lượng khách phải ít nhất là 1")
    private Integer guests;

    @Column(name = "total_price", nullable = false)
    @NotNull(message = "Tổng tiền không được để trống")
    @Min(value = 0, message = "Tổng tiền phải lớn hơn hoặc bằng 0")
    private Double totalPrice;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Trạng thái không được để trống")
    private BookingStatus status = BookingStatus.PENDING;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Payment payment;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<Additional> additionalServices = new ArrayList<>();

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "is_reviewed")
    private Boolean isReviewed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ServiceType {
        HOMESTAY("Homestay"),
        CAMPING("Cắm trại"),
        TRAVEL("Chuyến đi");

        private final String displayName;

        ServiceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum BookingStatus {
        PENDING("Chờ xác nhận"),
        CONFIRMED("Đã xác nhận"),
        PAID("Đã thanh toán"),
        COMPLETED("Đã hoàn thành"),
        CANCELLED("Đã hủy"),
        REFUNDED("Đã hoàn tiền");

        private final String displayName;

        BookingStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper methods
    public boolean canCancel() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    public boolean canReview() {
        return status == BookingStatus.COMPLETED && !isReviewed;
    }

    public boolean isPaid() {
        return status == BookingStatus.PAID || status == BookingStatus.COMPLETED;
    }

    public void addAdditionalService(Additional service) {
        additionalServices.add(service);
        service.setBooking(this);
    }

    public void removeAdditionalService(Additional service) {
        additionalServices.remove(service);
        service.setBooking(null);
    }

    public void setCancelled(boolean cancelled) {
        if (cancelled) {
            this.status = BookingStatus.CANCELLED;
        }
    }
}