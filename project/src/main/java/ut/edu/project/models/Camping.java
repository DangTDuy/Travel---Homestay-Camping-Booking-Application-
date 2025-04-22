package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "campings")
public class Camping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID duy nhất của khu cắm trại

    @NotBlank(message = "Tên không được để trống")
    private String name; // Tên khu cắm trại

    @NotBlank(message = "Địa điểm không được để trống")
    private String location; // Địa điểm khu cắm trại

    // Tọa độ GPS cho hiển thị bản đồ
    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả khu cắm trại

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private Double price; // Giá thuê khu cắm trại
    
    // Giá theo mùa
    private Double peakSeasonPrice; // Giá mùa cao điểm
    private Double lowSeasonPrice; // Giá mùa thấp điểm
    private Double weekendPrice; // Giá cuối tuần
    
    // Mùa cao điểm
    @Column(columnDefinition = "TEXT")
    private String peakSeasonInfo; // Thông tin về mùa cao điểm
    
    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải ít nhất là 1")
    private Integer capacity; // Sức chứa tối đa (thay thế cho maxPlaces)
    
    @NotNull(message = "Số chỗ có sẵn không được để trống")
    @Min(value = 0, message = "Số chỗ có sẵn không được âm")
    private Integer availableSlots; // Số chỗ có sẵn hiện tại

    // Trạng thái của khu cắm trại
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CampingStatus status = CampingStatus.OPEN; // Trạng thái mặc định là mở cửa
    
    public enum CampingStatus {
        OPEN("Đang mở cửa"), 
        CLOSED("Đóng cửa"), 
        MAINTENANCE("Đang bảo trì"), 
        SEASONAL_CLOSE("Đóng cửa theo mùa"),
        FULLY_BOOKED("Đã đặt hết");
        
        private final String displayName;
        
        CampingStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    @Column(nullable = false)
    private boolean isAvailable = true; // Trạng thái sẵn sàng của khu cắm trại

    @ElementCollection
    @CollectionTable(name = "camping_images", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "image_name")
    private List<String> images = new ArrayList<>(); // Danh sách tên file ảnh

    @ElementCollection
    @CollectionTable(name = "camping_image_urls", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "image_url")
    @Size(max = 5, message = "Tối đa 5 URL ảnh")
    private List<String> imageUrls = new ArrayList<>(); // Danh sách URL ảnh
    
    // URL video giới thiệu
    private String videoUrl;

    @ElementCollection
    @CollectionTable(name = "camping_facilities", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "facility")
    private List<String> facilities = new ArrayList<>(); // Danh sách tiện ích
    
    @ElementCollection
    @CollectionTable(name = "camping_equipment", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "equipment")
    private List<String> equipment = new ArrayList<>(); // Danh sách thiết bị có sẵn
    
    // Danh sách hoạt động giải trí
    @ElementCollection
    @CollectionTable(name = "camping_activities", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "activity")
    private List<String> activities = new ArrayList<>();
    
    // Danh sách điểm tham quan lân cận
    @ElementCollection
    @CollectionTable(name = "camping_attractions", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "attraction")
    private List<String> nearbyAttractions = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "camping_rules", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "rule")
    private List<String> rules = new ArrayList<>(); // Danh sách quy tắc

    // Thông tin y tế và an toàn
    @Column(columnDefinition = "TEXT")
    private String safetyInfo;

    @OneToMany(mappedBy = "camping", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>(); // Danh sách đặt chỗ

    @OneToMany(mappedBy = "camping", cascade = CascadeType.ALL)
    private List<Additional> additionalServices = new ArrayList<>(); // Danh sách dịch vụ bổ sung

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // Chủ sở hữu khu cắm trại

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer bookingCount = 0; // Số lượng đặt chỗ

    private Double rating; // Điểm đánh giá trung bình

    @Column(columnDefinition = "TEXT")
    private String terrain; // Địa hình khu cắm trại

    @Column(columnDefinition = "TEXT")
    private String weather; // Thông tin thời tiết
    
    // Mức độ khó tiếp cận (1-5)
    private Integer accessibilityLevel;
    // Mô tả tiếp cận
    @Column(columnDefinition = "TEXT")
    private String accessibilityDescription;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Thời gian tạo

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời gian cập nhật
    
    // Mùa tốt nhất để đến
    @Column(columnDefinition = "TEXT")
    private String bestSeasons;

    // Các trường mới cho đánh giá
    @ElementCollection
    @CollectionTable(name = "camping_reviews", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "review")
    private List<String> reviews = new ArrayList<>(); // Danh sách đánh giá

    @ElementCollection
    @CollectionTable(name = "camping_review_ratings", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "rating")
    private List<Integer> reviewRatings = new ArrayList<>(); // Danh sách điểm đánh giá

    @ElementCollection
    @CollectionTable(name = "camping_review_replies", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "reply")
    private List<String> reviewReplies = new ArrayList<>(); // Danh sách phản hồi đánh giá

    // Các trường mới cho thông báo
    @ElementCollection
    @CollectionTable(name = "camping_notifications", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "notification")
    private List<String> notifications = new ArrayList<>(); // Danh sách thông báo

    @Column
    private LocalDateTime lastNotificationSent; // Thời gian gửi thông báo cuối cùng

    // Các trường mới cho gợi ý
    @ElementCollection
    @CollectionTable(name = "camping_search_history", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "search_term")
    private List<String> searchHistory = new ArrayList<>(); // Lịch sử tìm kiếm

    // Trường mới cho thanh toán
    @Column
    private String lastPaymentStatus; // Trạng thái thanh toán cuối cùng

    @PrePersist
    protected void onCreate() {
        // Khởi tạo thời gian tạo và cập nhật khi tạo mới
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (bookingCount == null) bookingCount = 0;
        if (rating == null) rating = 0.0;
        if (availableSlots == null) availableSlots = capacity;
    }

    @PreUpdate
    protected void onUpdate() {
        // Cập nhật thời gian khi có thay đổi
        updatedAt = LocalDateTime.now();
    }

    // Phương thức hỗ trợ cho đặt chỗ
    public void addBooking(Booking booking) {
        // Thêm một đặt chỗ mới
        bookings.add(booking);
        booking.setCamping(this);
        bookingCount++;
        // Giảm số chỗ có sẵn
        if (availableSlots > 0) {
            availableSlots--;
        }
        // Kiểm tra nếu hết chỗ thì đổi trạng thái
        if (availableSlots == 0) {
            status = CampingStatus.FULLY_BOOKED;
        }
        addNotification("Đặt chỗ mới được thêm cho " + name);
    }

    public void removeBooking(Booking booking) {
        // Xóa một đặt chỗ
        bookings.remove(booking);
        booking.setCamping(null);
        bookingCount--;
        // Tăng số chỗ có sẵn
        if (availableSlots < capacity) {
            availableSlots++;
        }
        // Nếu có chỗ trống và trạng thái là FULLY_BOOKED thì đổi lại OPEN
        if (availableSlots > 0 && status == CampingStatus.FULLY_BOOKED) {
            status = CampingStatus.OPEN;
        }
        addNotification("Đặt chỗ đã bị xóa cho " + name);
    }

    // Phương thức hỗ trợ cho dịch vụ bổ sung
    public void addAdditionalService(Additional service) {
        // Thêm dịch vụ bổ sung
        additionalServices.add(service);
        service.setCamping(this);
    }

    public void removeAdditionalService(Additional service) {
        // Xóa dịch vụ bổ sung
        additionalServices.remove(service);
        service.setCamping(null);
    }
    
    // Thêm hoạt động giải trí
    public void addActivity(String activity) {
        if (activities == null) activities = new ArrayList<>();
        activities.add(activity);
    }
    
    // Thêm điểm tham quan lân cận
    public void addNearbyAttraction(String attraction) {
        if (nearbyAttractions == null) nearbyAttractions = new ArrayList<>();
        nearbyAttractions.add(attraction);
    }

    // Phương thức hỗ trợ cho đánh giá
    public void addReview(String review, Integer rating, String reply) {
        // Thêm một đánh giá mới
        reviews.add(review);
        reviewRatings.add(rating);
        reviewReplies.add(reply != null ? reply : "");
        updateRating();
        addNotification("Đánh giá mới được thêm cho " + name + ": " + rating + " sao");
    }

    public void replyToReview(int reviewIndex, String reply) {
        // Phản hồi một đánh giá
        if (reviewIndex >= 0 && reviewIndex < reviews.size()) {
            reviewReplies.set(reviewIndex, reply);
            addNotification("Chủ sở hữu đã trả lời một đánh giá cho " + name);
        }
    }

    public void updateRating() {
        // Cập nhật điểm đánh giá trung bình
        if (reviewRatings.isEmpty()) {
            rating = 0.0;
        } else {
            rating = reviewRatings.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
        }
    }

    // Phương thức hỗ trợ cho thông báo
    public void addNotification(String message) {
        // Thêm một thông báo mới
        notifications.add(message + " vào lúc " + LocalDateTime.now());
        lastNotificationSent = LocalDateTime.now();
    }

    public List<String> getRecentNotifications(int limit) {
        // Lấy danh sách thông báo gần đây
        return notifications.stream()
                .sorted((a, b) -> b.compareTo(a))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Phương thức hỗ trợ cho gợi ý
    public void addSearchTerm(String term) {
        // Thêm một từ khóa tìm kiếm
        if (!searchHistory.contains(term)) {
            searchHistory.add(term);
        }
    }

    public List<String> getSuggestedTerms() {
        // Lấy danh sách từ khóa gợi ý
        return new ArrayList<>(searchHistory);
    }

    // Phương thức hỗ trợ cho thanh toán
    public void setPaymentStatus(String status) {
        // Cập nhật trạng thái thanh toán
        this.lastPaymentStatus = status;
        addNotification("Trạng thái thanh toán được cập nhật: " + status);
    }
    
    // Phương thức tiện ích để kiểm tra xem có thể đánh giá hay không
    public boolean isReviewable() {
        return isAvailable && status == CampingStatus.OPEN;
    }
    
    // Phương thức tiện ích để lấy số lượng đánh giá
    public int getReviewCount() {
        return reviews != null ? reviews.size() : 0;
    }
    
    // Phương thức tiện ích để lấy giá theo loại thời gian
    public Double getPriceBySeasonAndDay(boolean isPeakSeason, boolean isWeekend) {
        if (isPeakSeason) {
            return peakSeasonPrice != null ? peakSeasonPrice : price;
        } else if (isWeekend) {
            return weekendPrice != null ? weekendPrice : price;
        } else {
            return lowSeasonPrice != null ? lowSeasonPrice : price;
        }
    }

    public void setIsAvailable(boolean available) {
        this.isAvailable = available;
        if (!available && status == CampingStatus.OPEN) {
            status = CampingStatus.CLOSED;
        }
    }

    @Deprecated
    public Integer getMaxPlaces() {
        return capacity;
    }

    @Deprecated
    public void setMaxPlaces(Integer maxPlaces) {
        this.capacity = maxPlaces;
    }
}