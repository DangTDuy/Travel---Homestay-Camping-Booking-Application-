package ut.edu.project.models;

import jakarta.persistence.*;
import lombok.*;

@Entity // Đánh dấu đây là một thực thể JPA
@Table(name = "reviews") // Tên bảng trong cơ sở dữ liệu
@Getter // Tự động tạo getter (Lombok)
@Setter // Tự động tạo setter (Lombok)
@NoArgsConstructor // Tự động tạo constructor không tham số (Lombok)
@AllArgsConstructor // Tự động tạo constructor đầy đủ tham số (Lombok)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID tự động tăng
    private Long id;

    @ManyToOne // Quan hệ nhiều-một với User (người đánh giá)
    @JoinColumn(name = "user_id", nullable = false) // Cột khóa ngoại liên kết với bảng users
    private User user; // Người đánh giá

    @Column(name = "service_type", nullable = false) // Loại hình dịch vụ
    private String serviceType; // "Homestay", "Camping", hoặc "Travel"

    @Column(name = "service_id", nullable = false) // ID của dịch vụ được đánh giá
    private Long serviceId; // ID của Homestay, Camping, hoặc Travel

    @Column(name = "rating", nullable = false) // Số sao đánh giá (1-5)
    private Integer rating; // Giá trị từ 1 đến 5

    @Column(name = "comment") // Bình luận (có thể để trống)
    private String comment; // Nội dung bình luận của khách
}