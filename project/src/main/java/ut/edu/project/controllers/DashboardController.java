package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ut.edu.project.models.Homestay;
import ut.edu.project.services.AIRecommendationService;

import java.util.List;

@Controller
public class DashboardController {

    private final AIRecommendationService aiRecommendationService;

    @Autowired
    public DashboardController(AIRecommendationService aiRecommendationService) {
        this.aiRecommendationService = aiRecommendationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Add username to model
            String username = authentication.getName();
            model.addAttribute("username", username);

            // Check if user has ADMIN role
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ADMIN"::equals);
            model.addAttribute("isAdmin", isAdmin);

            // Get AI recommendations
            List<Homestay> recommendedHomestays = aiRecommendationService.recommendHomestays(username);
            model.addAttribute("recommendedHomestays", recommendedHomestays);
        }
        return "dashboard";
    }
    @GetMapping("/dashboard/recommendations")
    public String getRecommendations(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            List<Homestay> recommendedHomestays = aiRecommendationService.recommendHomestays(authentication.getName());
            model.addAttribute("recommendedHomestays", recommendedHomestays);
        }
        return "dashboard :: #recommendations-section"; // Chỉ trả về fragment
    }
}