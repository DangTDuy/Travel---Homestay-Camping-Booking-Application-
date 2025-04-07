package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ut.edu.project.dtos.RegisterDTO;
import ut.edu.project.dtos.UserDTO;
import ut.edu.project.dtos.UpdateUserDTO;
import ut.edu.project.models.User;
import ut.edu.project.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", username);
                    return new UsernameNotFoundException("Không tìm thấy người dùng với tên đăng nhập: " + username);
                });

        logger.info("User found: {} with role: {}", username, user.getRole());
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole()))
        );
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public String registerUser(RegisterDTO registerDTO) {
        if (registerDTO == null) {
            return "Dữ liệu đăng ký không hợp lệ.";
        }

        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            return "Tên đăng nhập đã tồn tại.";
        }

        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            return "Email đã được sử dụng.";
        }

        // Thêm kiểm tra số điện thoại
        if (registerDTO.getSoDienThoai() != null && !registerDTO.getSoDienThoai().isEmpty()
                && userRepository.existsBySoDienThoai(registerDTO.getSoDienThoai())) {
            return "Số điện thoại đã được sử dụng.";
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername().trim());
        user.setHoTen(registerDTO.getHoTen().trim());
        user.setEmail(registerDTO.getEmail().trim().toLowerCase());
        user.setSoDienThoai(registerDTO.getSoDienThoai() != null ? registerDTO.getSoDienThoai().trim() : null);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        // Chuẩn hóa role thành chữ hoa và mặc định là "USER" nếu null
        String role = registerDTO.getRole() != null ? registerDTO.getRole().toUpperCase() : "USER";
        user.setRole(role);

        try {
            userRepository.save(user);
            return "Đăng ký thành công.";
        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    public boolean hasRole(String username, String role) {
        User user = findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));
        return user.getRole().equals(role);
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        userRepository.delete(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng.")));
    }

    public UserDTO updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));
        user.setUsername(updatedUser.getUsername());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        user.setRole(updatedUser.getRole());
        userRepository.save(user);
        return new UserDTO(user);
    }
    public UserDTO saveUser(UserDTO userDTO) {
        User user;
        if (userDTO.getId() != null) {
            // Cập nhật user hiện có
            user = userRepository.findById(userDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));
        } else {
            // Tạo user mới
            if (userRepository.existsByUsername(userDTO.getUsername())) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại.");
            }
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng.");
            }
            user = new User();
        }

        // Cập nhật thông tin user
        user.setUsername(userDTO.getUsername());
        user.setHoTen(userDTO.getHoTen());
        user.setEmail(userDTO.getEmail());
        user.setSoDienThoai(userDTO.getSoDienThoai());
        user.setRole(userDTO.getRole());

        // Nếu là tạo mới, cần set password mặc định
        if (userDTO.getId() == null) {
            user.setPassword(passwordEncoder.encode("password123")); // Password mặc định, có thể thay đổi
        }

        user = userRepository.save(user);
        return new UserDTO(user);
    }
    public UserDTO updateUserByUsername(String username, UpdateUserDTO updatedUserDTO) {
        User user = findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));
        if (updatedUserDTO.getHoTen() != null) {
            user.setHoTen(updatedUserDTO.getHoTen());
        }
        if (updatedUserDTO.getEmail() != null) {
            user.setEmail(updatedUserDTO.getEmail());
        }
        if (updatedUserDTO.getSoDienThoai() != null) {
            user.setSoDienThoai(updatedUserDTO.getSoDienThoai());
        }
        userRepository.save(user);
        return new UserDTO(user);
    }

    public UserDTO getUserById(Long id) {
        return new UserDTO(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng.")));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }
}