package ut.edu.project.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/admin/test")
    public String adminTest(@AuthenticationPrincipal UserDetails userDetails) {
        return "Welcome Admin: " + userDetails.getUsername();
    }

    @GetMapping("/user/test")
    public String userTest(@AuthenticationPrincipal UserDetails userDetails) {
        return "Welcome User: " + userDetails.getUsername();
    }

    @GetMapping("/public")
    public String publicTest() {
        return "This is public!";
    }
}