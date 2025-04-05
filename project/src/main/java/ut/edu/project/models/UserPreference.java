package ut.edu.project.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "user_preferences")
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Sở thích về loại homestay
    private String preferredLocation; // Vị trí ưa thích
    private Double preferredPriceRange; // Khoảng giá ưa thích
    
    @Column(columnDefinition = "TEXT")
    private String preferredAmenities; // Tiện nghi ưa thích (dạng JSON string)
    
    @Column(columnDefinition = "TEXT")
    private String preferredTags; // Tags ưa thích (dạng JSON string)

    // Thống kê lịch sử
    private Integer totalBookings; // Tổng số lần đặt phòng
    private Double averageRating; // Đánh giá trung bình
    
    @Column(columnDefinition = "TEXT")
    private String lastVisitedLocations; // Các địa điểm đã ghé thăm (dạng JSON string)

    // Thời gian và mùa
    @Column(columnDefinition = "TEXT")
    private String preferredSeasons; // Mùa ưa thích (dạng JSON string)
    private String preferredDuration; // Thời gian lưu trú ưa thích

    // Các yếu tố khác
    private Boolean prefersQuiet; // Thích yên tĩnh
    private Boolean prefersFamily; // Thích phù hợp gia đình
    private Boolean prefersLuxury; // Thích sang trọng
    private Boolean prefersBudget; // Thích tiết kiệm

    @Column(columnDefinition = "TEXT")
    private String aiRecommendations; // Lưu các gợi ý từ AI (dạng JSON string)
} 