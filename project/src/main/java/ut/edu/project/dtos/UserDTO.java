package ut.edu.project.dtos;

import ut.edu.project.models.User; // Import lớp User nếu cần

public class UserDTO {
    private Long id;
    private String username;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String role;

    public UserDTO() {}

    public UserDTO(Long id, String username, String hoTen, String email, String soDienThoai, String role) {
        this.id = id;
        this.username = username;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.role = role;
    }

    // Constructor nhận vào đối tượng User
    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.hoTen = user.getHoTen();
        this.email = user.getEmail();
        this.soDienThoai = user.getSoDienThoai();
        this.role = user.getRole();
    }

    // Getters và setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}