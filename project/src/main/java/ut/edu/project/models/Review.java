package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

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
    
    @Column(name = "owner_reply", columnDefinition = "TEXT")
    private String ownerReply;
    
    @Column(name = "owner_reply_date")
    private LocalDateTime ownerReplyDate;

    @ElementCollection
    @CollectionTable(name = "review_likes", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "username")
    private Set<String> likedBy = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "review_reports", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "username")
    private Set<String> reportedBy = new HashSet<>();

    private String reportReason;

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

    public void setTravelId(Long travelId) {
        if (this.travel == null) {
            this.travel = new Travel();
        }
        this.travel.setId(travelId);
    }

    public void setUsername(String username) {
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setUsername(username);
    }

    public Set<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(Set<String> likedBy) {
        this.likedBy = likedBy;
    }

    public Set<String> getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Set<String> reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getReportReason() {
        return reportReason;
    }

    public void setReportReason(String reportReason) {
        this.reportReason = reportReason;
    }
}