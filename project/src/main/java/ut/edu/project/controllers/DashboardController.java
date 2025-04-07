package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ut.edu.project.models.*;
import ut.edu.project.repositories.BookingRepository;
import ut.edu.project.repositories.HomestayRepository;
import ut.edu.project.repositories.CampingRepository;
import ut.edu.project.repositories.TravelRepository;
import ut.edu.project.repositories.UserRepository;
import ut.edu.project.services.AIRecommendationService;
import ut.edu.project.services.BookingService;
import ut.edu.project.services.UserService;
import ut.edu.project.services.TravelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private AIRecommendationService aiRecommendationService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TravelService travelService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private CampingRepository campingRepository;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
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
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            boolean isAdmin = user.getRole().equals("ADMIN");
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("currentUser", user);

            if (isAdmin) {
                logger.info("Loading data for ADMIN dashboard");
                List<Booking> recentBookings = bookingRepository.findTop10ByOrderByCreatedAtDesc();
                model.addAttribute("recentBookings", recentBookings != null ? recentBookings : new ArrayList<>());

                model.addAttribute("totalUsers", userRepository.count());
                model.addAttribute("totalHomestays", homestayRepository.count());
                model.addAttribute("totalCampings", campingRepository.count());
                model.addAttribute("totalTravels", travelRepository.count());
                model.addAttribute("totalBookings", bookingRepository.count());

                List<Object[]> statusCounts = bookingRepository.countBookingsByStatus();
                Map<Booking.BookingStatus, Long> bookingStatusMap = statusCounts != null ?
                        statusCounts.stream()
                                .collect(Collectors.toMap(
                                        row -> (Booking.BookingStatus) row[0],
                                        row -> (Long) row[1]
                                )) : new HashMap<>();
                model.addAttribute("bookingStatusCounts", bookingStatusMap);
                model.addAttribute("bookingStatuses", Booking.BookingStatus.values());

            } else {
                logger.info("Loading data for USER dashboard for user: {}", username);
                List<Booking> recentUserBookings = bookingService.getTop3BookingsByUsername(username);
                model.addAttribute("recentUserBookings", recentUserBookings != null ? recentUserBookings : new ArrayList<>());

                try {
                    model.addAttribute("recommendedHomestays",
                            aiRecommendationService.generateRecommendations(user, 3, "personalized"));
                } catch (Exception e) {
                    logger.error("Error generating recommendations: {}", e.getMessage());
                    model.addAttribute("recommendedHomestays", new ArrayList<>());
                }

                try {
                    List<Travel> recommendedTravels = travelService.getRecentTravels();
                    model.addAttribute("recommendedTravels", recommendedTravels != null ? recommendedTravels : new ArrayList<>());
                } catch (Exception e) {
                    logger.error("Error getting recent travels: {}", e.getMessage());
                    model.addAttribute("recommendedTravels", new ArrayList<>());
                }
            }

            return "dashboard";
        } catch (Exception e) {
            logger.error("Error loading dashboard: {}", e.getMessage(), e);
            model.addAttribute("error", "Có lỗi xảy ra khi tải trang tổng quan. Vui lòng thử lại sau.");
            return "error";
        }
    }

    @GetMapping("/dashboard/recommendations")
    public String getRecommendations(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return "fragments/empty :: empty";
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return "fragments/empty :: empty";
        }

        // Cung cấp khuyến nghị cho tất cả người dùng, bất kể vai trò
        model.addAttribute("recommendedHomestays",
                aiRecommendationService.generateRecommendations(user, 3, "personalized"));
        return "dashboard :: #recommendations-content";
    }
}