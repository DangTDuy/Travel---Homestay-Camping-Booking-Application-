package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ut.edu.project.models.User;
import ut.edu.project.services.UserService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public String adminTest(Authentication authentication) {
        return "Welcome Admin: " + authentication.getName();
    }

    @GetMapping("/profile")
    public String getUserProfile(Authentication authentication) {
        String username = authentication.getName();
        // Lấy thông tin user từ database
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return "Admin Profile: Username=" + user.getUsername() + ", Role=" + user.getRole();
    }
}