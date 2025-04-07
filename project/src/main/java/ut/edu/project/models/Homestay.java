package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "homestays")
public class Homestay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Tên homestay không được để trống")
    @Size(min = 5, max = 100, message = "Tên homestay phải từ 5 đến 100 ký tự")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Địa điểm không được để trống")
    private String location;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Giá mỗi đêm không được để trống")
    @Positive(message = "Giá mỗi đêm phải là số dương")
    private Double price;

    @Column(nullable = false)
    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải ít nhất là 1")
    private Integer capacity;

    @ElementCollection
    @CollectionTable(name = "homestays_amenities", joinColumns = @JoinColumn(name = "homestay_id"))
    @Column(name = "amenity")
    @Size(max = 10, message = "Tối đa 10 tiện nghi")
    private List<String> amenities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "homestays_image_urls", joinColumns = @JoinColumn(name = "homestay_id"))
    @Column(name = "image_url")
    @Size(max = 5, message = "Tối đa 5 URL ảnh")
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "homestays_images", joinColumns = @JoinColumn(name = "homestay_id"))
    @Column(name = "image_path")
    @Size(max = 5, message = "Tối đa 5 ảnh")
    private List<String> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    @NotNull(message = "Chủ sở hữu không được để trống")
    private User owner;

    @OneToMany(mappedBy = "homestay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "homestay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Integer booking_count = 0;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String seasons;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (booking_count == null) {
            booking_count = 0;
        }
        if (rating == null) {
            rating = 0.0;
        }
        if (amenities == null) {
            amenities = new ArrayList<>();
        }
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        if (images == null) {
            images = new ArrayList<>();
        }
        if (bookings == null) {
            bookings = new ArrayList<>();
        }
        if (reviews == null) {
            reviews = new ArrayList<>();
        }
    }

    // Helper methods for JSON handling
    public List<String> getTagsList() {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(tags, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void setTagsList(List<String> tagsList) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.tags = mapper.writeValueAsString(tagsList);
        } catch (Exception e) {
            this.tags = "[]";
        }
    }

    public List<String> getSeasonsList() {
        if (seasons == null || seasons.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(seasons, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void setSeasonsList(List<String> seasonsList) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.seasons = mapper.writeValueAsString(seasonsList);
        } catch (Exception e) {
            this.seasons = "[]";
        }
    }
}