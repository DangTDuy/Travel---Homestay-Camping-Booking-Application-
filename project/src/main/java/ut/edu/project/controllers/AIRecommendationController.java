package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;
import ut.edu.project.models.UserPreference;
import ut.edu.project.services.AIRecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class AIRecommendationController {

    @Autowired
    private AIRecommendationService aiRecommendationService;

    @GetMapping("/preferences")
    public ResponseEntity<UserPreference> getUserPreferences(@AuthenticationPrincipal User user) {
        UserPreference preferences = aiRecommendationService.analyzeUserPreferences(user);
        return ResponseEntity.ok(preferences);
    }

    @GetMapping("/homestays")
    public ResponseEntity<List<Homestay>> getRecommendedHomestays(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "5") int limit) {
        List<Homestay> recommendations = aiRecommendationService.generateRecommendations(user, limit);
        return ResponseEntity.ok(recommendations);
    }
} 