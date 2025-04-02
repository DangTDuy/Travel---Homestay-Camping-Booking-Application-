package ut.edu.project.dtos;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String role;
    private String hoTen; // Thêm trường hoTen
    private String email;  // Thêm trường email
    private String soDienThoai; // Thêm trường soDienThoai

    // Lombok @Data sẽ tự động tạo getters, setters, equals(), hashCode(), toString()
}