package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "bookings")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "travels")
public class Travel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Tên tour không được để trống")
    @Size(min = 5, message = "Tên tour phải có ít nhất 5 ký tự")
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

    @NotBlank(message = "Lịch trình không được để trống")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String itinerary;

    @ManyToOne
    @JoinColumn(name = "guide_id", nullable = false)
    @NotNull(message = "Hướng dẫn viên không được để trống")
    private User guide;

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    @Min(value = 1, message = "Số người tham gia tối đa phải ít nhất là 1")
    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Min(value = 1, message = "Số người tham gia tối thiểu phải ít nhất là 1")
    @Column(name = "min_participants")
    private Integer minParticipants;

    @Min(value = 1, message = "Số ngày tour phải ít nhất là 1")
    @Column(name = "duration_days")
    private Integer durationDays;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "travel_image_urls", joinColumns = @JoinColumn(name = "travel_id"))
    @Column(name = "image_url")
    @Size(max = 5, message = "Tối đa 5 URL ảnh")
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "travel_included_services", joinColumns = @JoinColumn(name = "travel_id"))
    @Column(name = "service")
    private List<String> includedServices = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String requirements; // Yêu cầu đối với người tham gia

    @Column(columnDefinition = "TEXT")
    private String highlights; // Điểm nhấn của tour

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel = DifficultyLevel.NORMAL;

    @Column(nullable = false)
    private boolean isAvailable = true;

    @Column(nullable = false)
    private Integer bookingCount = 0;

    @Column(nullable = false)
    private Double rating = 0.0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
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

    public int getAvailableSlots() {
        return maxParticipants - bookings.size();
    }
}
