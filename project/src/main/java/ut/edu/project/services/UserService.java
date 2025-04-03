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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với tên đăng nhập: " + username));
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
        user.setRole(registerDTO.getRole() != null ? registerDTO.getRole() : "USER");

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
}