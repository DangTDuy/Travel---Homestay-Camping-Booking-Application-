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
@Table(name = "campings")
public class Camping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên không được để trống")
    private String name;

    @NotBlank(message = "Địa điểm không được để trống")
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private Double price;

    @NotNull(message = "Số chỗ không được để trống")
    @Min(value = 1, message = "Số chỗ phải ít nhất là 1")
    private Integer maxPlaces;

    @Column(nullable = false)
    private boolean isAvailable = true;

    @ElementCollection
    @CollectionTable(name = "camping_image_urls", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "image_url")
    @Size(max = 5, message = "Tối đa 5 URL ảnh")
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "camping_facilities", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "facility")
    private List<String> facilities = new ArrayList<>();

    @OneToMany(mappedBy = "camping", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "camping", cascade = CascadeType.ALL)
    private List<Additional> additionalServices = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer bookingCount = 0;

    private Double rating;

    @Column(columnDefinition = "TEXT")
    private String rules; // Quy tắc cắm trại

    @Column(columnDefinition = "TEXT")
    private String terrain; // Địa hình khu cắm trại

    @Column(columnDefinition = "TEXT")
    private String weather; // Thông tin thời tiết

    @Column(columnDefinition = "TEXT")
    private String equipment; // Trang thiết bị có sẵn

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
        booking.setCamping(this);
        bookingCount++;
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setCamping(null);
        bookingCount--;
    }

    public void addAdditionalService(Additional service) {
        additionalServices.add(service);
        service.setCamping(this);
    }

    public void removeAdditionalService(Additional service) {
        additionalServices.remove(service);
        service.setCamping(null);
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}