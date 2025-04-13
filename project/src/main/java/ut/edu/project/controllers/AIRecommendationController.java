package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;
import ut.edu.project.models.UserPreference;
import ut.edu.project.services.AIRecommendationService;
import ut.edu.project.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class AIRecommendationController {
    private static final Logger logger = LoggerFactory.getLogger(AIRecommendationController.class);

    @Autowired
    private AIRecommendationService aiRecommendationService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated");
        }

        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/preferences")
    public ResponseEntity<?> getUserPreferences() {
        try {
            User user = getCurrentUser();
            UserPreference preferences = aiRecommendationService.analyzeUserPreferences(user);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            logger.error("Error getting user preferences", e);
            return ResponseEntity.internalServerError().body("Error getting user preferences: " + e.getMessage());
        }
    }

    @GetMapping("/homestays")
    public ResponseEntity<?> getRecommendedHomestays(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String type) {
        try {
            User user = getCurrentUser();
            List<Homestay> recommendations = aiRecommendationService.generateRecommendations(user, limit, type);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Error getting recommended homestays", e);
            return ResponseEntity.internalServerError().body("Error getting recommendations: " + e.getMessage());
        }
    }

    @GetMapping("/homestays/filter")
    public ResponseEntity<?> getFilteredRecommendations(
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice,
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "amenities", required = false) List<String> amenities,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        try {
            User user = getCurrentUser();
            List<Homestay> recommendations = aiRecommendationService.getFilteredRecommendations(
                user, minPrice, maxPrice, location, amenities, limit);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Error getting filtered recommendations", e);
            return ResponseEntity.internalServerError().body("Error getting filtered recommendations: " + e.getMessage());
        }
    }

    @GetMapping("/homestays/trending")
    public ResponseEntity<?> getTrendingHomestays(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Homestay> trending = aiRecommendationService.getTrendingHomestays(limit);
            return ResponseEntity.ok(trending);
        } catch (Exception e) {
            logger.error("Error getting trending homestays", e);
            return ResponseEntity.internalServerError().body("Error getting trending homestays: " + e.getMessage());
        }
    }

    @GetMapping("/homestays/deals")
    public ResponseEntity<?> getDealHomestays(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Homestay> deals = aiRecommendationService.getDealHomestays(limit);
            return ResponseEntity.ok(deals);
        } catch (Exception e) {
            logger.error("Error getting deal homestays", e);
            return ResponseEntity.internalServerError().body("Error getting deal homestays: " + e.getMessage());
        }
    }

    @PutMapping("/preferences")
    public ResponseEntity<?> updateUserPreferences(
            @RequestBody Map<String, Object> preferences) {
        try {
            User user = getCurrentUser();
            UserPreference updatedPreferences = aiRecommendationService.updateUserPreferences(user, preferences);
            return ResponseEntity.ok(updatedPreferences);
        } catch (Exception e) {
            logger.error("Error updating user preferences", e);
            return ResponseEntity.internalServerError().body("Error updating preferences: " + e.getMessage());
        }
    }
}