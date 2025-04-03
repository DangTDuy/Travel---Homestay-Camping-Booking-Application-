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
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole()))  // No "ROLE_" prefix
        );
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public String registerUser(RegisterDTO registerDTO) {
        if (registerDTO == null) {
            return "Invalid input";
        }
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            return "User already exists!";
        }
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setHoTen(registerDTO.getHoTen());
        user.setEmail(registerDTO.getEmail());
        user.setSoDienThoai(registerDTO.getSoDienThoai());
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());
        System.out.println("Encoded password for " + registerDTO.getUsername() + ": " + encodedPassword);
        user.setPassword(encodedPassword);
        user.setRole(registerDTO.getRole());
        userRepository.save(user);
        return "User registered successfully!";
    }

    public boolean hasRole(String username, String role) {
        User user = findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRole().equals(role);
    }

    public List<UserDTO> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> new UserDTO(
                user.getId(), // Thêm id
                user.getUsername(),
                user.getHoTen(),
                user.getEmail(),
                user.getSoDienThoai(),
                user.getRole()
        )).collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public UserDTO updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(updatedUser.getUsername());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        user.setRole(updatedUser.getRole());
        userRepository.save(user);
        return new UserDTO(user.getId(), user.getUsername(), user.getHoTen(), user.getEmail(), user.getSoDienThoai(), user.getRole());
    }

    public UserDTO updateUserByUsername(String username, UpdateUserDTO updatedUserDTO) {
        User user = findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
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
        return new UserDTO(user.getId(), user.getUsername(), user.getHoTen(), user.getEmail(), user.getSoDienThoai(), user.getRole());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDTO(user.getId(), user.getUsername(), user.getHoTen(), user.getEmail(), user.getSoDienThoai(), user.getRole());
    }
}