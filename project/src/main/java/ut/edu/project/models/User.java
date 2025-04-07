package ut.edu.project.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Getter
@Setter
@Table(name = "users")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private String role;

    private String hoTen;
    private String email;
    private String soDienThoai;

    // Hằng số cho roles
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MODERATOR = "MODERATOR";
    // Thêm các roles khác nếu cần

    // Constructors (nếu cần)
    public User() {}

    public User(String username, String password, String role, String hoTen, String email, String soDienThoai) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
    }

    // Getters and Setters (đã được lombok tạo tự động)
    // (Lombok @Getter và @Setter sẽ tự động tạo ra các getter và setter)
}