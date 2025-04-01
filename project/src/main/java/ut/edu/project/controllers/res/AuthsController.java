package ut.edu.project.controllers.res;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ut.edu.project.dtos.AuthResponse;
import ut.edu.project.dtos.LoginRequest;
import ut.edu.project.dtos.RegisterDTO;
import ut.edu.project.jwt.JwtUtil;
import ut.edu.project.services.UserService;

@RestController
@RequestMapping("/auths")
public class AuthsController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
            // Lấy role từ UserDetails
            String role = userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElse("USER"); // Mặc định là "USER" nếu không tìm thấy role
            String token = jwtUtil.generateToken(userDetails.getUsername(), role);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai thông tin đăng nhập");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterDTO registerDTO) {
        String result = userService.registerUser(registerDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token!");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token!");
        }

        String username = jwtUtil.extractUsername(token);
        return ResponseEntity.ok("User: " + username);
    }
}