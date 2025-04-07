package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
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

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    @NotNull(message = "Booking không được để trống")
    private Booking booking;

    @Column(nullable = false)
    @NotNull(message = "Điểm đánh giá không được để trống")
    @Min(value = 1, message = "Điểm đánh giá phải từ 1 đến 5")
    @Max(value = 5, message = "Điểm đánh giá phải từ 1 đến 5")
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @ElementCollection
    @CollectionTable(name = "review_images", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url")
    @Size(max = 5, message = "Tối đa 5 ảnh")
    private List<String> images = new ArrayList<>();

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

    // Helper methods
    public String getServiceType() {
        if (homestay != null) return "HOMESTAY";
        if (camping != null) return "CAMPING";
        if (travel != null) return "TRAVEL";
        return null;
    }

    public Long getServiceId() {
        if (homestay != null) return homestay.getId();
        if (camping != null) return camping.getId();
        if (travel != null) return travel.getId();
        return null;
    }

    public String getServiceName() {
        if (homestay != null) return homestay.getName();
        if (camping != null) return camping.getName();
        if (travel != null) return travel.getTourName();
        return null;
    }
}