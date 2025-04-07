package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "travels")
public class Travel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên tour không được để trống")
    @Column(name = "tour_name", nullable = false)
    private String tourName;

    @NotBlank(message = "Địa điểm không được để trống")
    @Column(nullable = false)
    private String location;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private Double price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Lịch trình không được để trống")
    private String itinerary;

    @ManyToOne
    @JoinColumn(name = "guide_id", nullable = false)
    @NotNull(message = "Hướng dẫn viên không được để trống")
    private User guide;

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    @Column(name = "max_participants")
    @Min(value = 1, message = "Số người tham gia tối đa phải ít nhất là 1")
    private Integer maxParticipants;

    @Column(name = "min_participants")
    @Min(value = 1, message = "Số người tham gia tối thiểu phải ít nhất là 1")
    private Integer minParticipants;

    @Column(name = "duration_days")
    @Min(value = 1, message = "Số ngày tour phải ít nhất là 1")
    private Integer durationDays;

    @ElementCollection
    @CollectionTable(name = "travel_image_urls", joinColumns = @JoinColumn(name = "travel_id"))
    @Column(name = "image_url")
    @Size(max = 5, message = "Tối đa 5 URL ảnh")
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "travel_included_services", joinColumns = @JoinColumn(name = "travel_id"))
    @Column(name = "service")
    private List<String> includedServices = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String requirements; // Yêu cầu đối với người tham gia

    @Column(columnDefinition = "TEXT")
    private String highlights; // Điểm nhấn của tour

    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel = DifficultyLevel.NORMAL;

    @Column(nullable = false)
    private boolean isAvailable = true;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer bookingCount = 0;

    private Double rating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DifficultyLevel {
        EASY("Dễ"),
        NORMAL("Trung bình"),
        CHALLENGING("Thử thách"),
        DIFFICULT("Khó");

        private final String displayName;

        DifficultyLevel(String displayName) {
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
        if (bookingCount == null) {
            bookingCount = 0;
        }
        if (rating == null) {
            rating = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setTravel(this);
        bookingCount++;
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setTravel(null);
        bookingCount--;
    }

    public boolean isFullyBooked() {
        return bookings.size() >= maxParticipants;
    }

    public boolean hasMinimumParticipants() {
        return bookings.size() >= minParticipants;
    }
}
