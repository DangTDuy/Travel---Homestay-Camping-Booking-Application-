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
@Table(name = "homestay")
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
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Giá mỗi đêm không được để trống")
    @Positive(message = "Giá mỗi đêm phải là số dương")
    private Double pricePerNight;

    @Column
    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải ít nhất là 1")
    private Integer capacity;

    @ElementCollection
    @CollectionTable(name = "homestay_amenities", joinColumns = @JoinColumn(name = "homestay_id"))
    @Column(name = "amenity")
    @Size(max = 10, message = "Tối đa 10 tiện nghi")
    private List<String> amenities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "homestay_image_urls", joinColumns = @JoinColumn(name = "homestay_id"))
    @Column(name = "image_url")
    @Size(max = 5, message = "Tối đa 5 URL ảnh")
    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER) // Thay LAZY thành EAGER
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}