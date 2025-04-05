package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ut.edu.project.models.User;
import ut.edu.project.services.AIRecommendationService;
import ut.edu.project.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private AIRecommendationService aiRecommendationService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Authentication: {}", authentication);
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            logger.info("User not authenticated, redirecting to login");
            return "redirect:/auth/login-user";
        }
        
        String username = authentication.getName();
        logger.info("Authenticated user: {}", username);
        
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Kiểm tra xem người dùng có phải là admin không
        boolean isAdmin = user.getRole().equals("ADMIN");
        model.addAttribute("isAdmin", isAdmin);
        
        // Lấy danh sách homestay được đề xuất từ AI
        model.addAttribute("recommendedHomestays", 
            aiRecommendationService.generateRecommendations(user, 6));
        return "dashboard";
    }

    @GetMapping("/dashboard/recommendations")
    public String getRecommendations(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/auth/login-user";
        }
        
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("recommendedHomestays", 
            aiRecommendationService.generateRecommendations(user, 6));
        return "dashboard :: #recommendations-content";
    }
}