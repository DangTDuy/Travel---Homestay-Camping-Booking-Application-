package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ut.edu.project.dtos.RegisterDTO;
import ut.edu.project.models.User;
import ut.edu.project.repositories.UserRepository;

import java.util.Collections;
import java.util.Optional;

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
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public String registerUser(RegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            return "User already exists!";
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());
        System.out.println("Encoded password for " + registerDTO.getUsername() + ": " + encodedPassword); // Debug log
        user.setPassword(encodedPassword);
        user.setRole(registerDTO.getRole());

        userRepository.save(user);
        return "User registered successfully!";
    }
}