package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Table(name = "campings")
public class Camping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID duy nhất của khu cắm trại

    @NotBlank(message = "Tên không được để trống")
    private String name; // Tên khu cắm trại

    @NotBlank(message = "Địa điểm không được để trống")
    private String location; // Địa điểm khu cắm trại

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả khu cắm trại

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private Double price; // Giá thuê khu cắm trại

    @NotNull(message = "Số chỗ không được để trống")
    @Min(value = 1, message = "Số chỗ phải ít nhất là 1")
    private Integer maxPlaces; // Số chỗ tối đa

    @Column(nullable = false)
    private boolean isAvailable = true; // Trạng thái sẵn sàng của khu cắm trại

    @ElementCollection
    @CollectionTable(name = "camping_image_urls", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "image_url")
    @Size(max = 5, message = "Tối đa 5 URL ảnh")
    private List<String> imageUrls = new ArrayList<>(); // Danh sách URL ảnh

    @ElementCollection
    @CollectionTable(name = "camping_facilities", joinColumns = @JoinColumn(name = "camping_id"))
    @Column(name = "facility")
    private List<String> facilities = new ArrayList<>(); // Danh sách tiện ích

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
    private String rules; // Quy tắc cắm trại

    @Column(columnDefinition = "TEXT")
    private String terrain; // Địa hình khu cắm trại

    @Column(columnDefinition = "TEXT")
    private String weather; // Thông tin thời tiết

    @Column(columnDefinition = "TEXT")
    private String equipment; // Trang thiết bị có sẵn

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Thời gian tạo

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời gian cập nhật

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
        addNotification("Đặt chỗ mới được thêm cho " + name);
    }

    public void removeBooking(Booking booking) {
        // Xóa một đặt chỗ
        bookings.remove(booking);
        booking.setCamping(null);
        bookingCount--;
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

    // Các phương thức tiện ích khác
    public boolean isReviewable() {
        // Kiểm tra xem khu cắm trại có thể được đánh giá không
        return !bookings.isEmpty();
    }

    public int getReviewCount() {
        // Lấy số lượng đánh giá
        return reviews.size();
    }

    public void setIsAvailable(boolean available) {
    }
}