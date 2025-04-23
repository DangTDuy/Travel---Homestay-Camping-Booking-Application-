package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "additionals")
public class Additional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "homestay_id")
    private Homestay homestay;

    @ManyToOne
    @JoinColumn(name = "camping_id")
    private Camping camping;

    @ManyToOne
    @JoinColumn(name = "travel_id")
    private Travel travel;

    // Không còn quan hệ trực tiếp với Booking nữa, thay vào đó sử dụng BookingAdditional
    // @ManyToOne
    // @JoinColumn(name = "booking_id")
    // private Booking booking;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    private boolean isActive = true;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Số lượng mặc định cho dịch vụ này khi hiển thị cho người dùng.
     * Người dùng có thể thay đổi số lượng khi đặt dịch vụ.
     * Được sử dụng để tính tổng giá trong getTotalPrice().
     */
    @Column(name = "quantity")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType = ServiceType.ALL;

    public enum ServiceType {
        HOMESTAY("Homestay"),
        CAMPING("Camping"),
        TRAVEL("Tour du lịch"),
        ALL("Tất cả");

        private final String displayName;

        ServiceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isAvailable(LocalTime requestTime) {
        return isActive && requestTime.isAfter(startTime) && requestTime.isBefore(endTime);
    }
}