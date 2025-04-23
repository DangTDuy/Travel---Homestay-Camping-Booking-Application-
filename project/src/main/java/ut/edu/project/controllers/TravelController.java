package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ut.edu.project.models.Review;
import ut.edu.project.models.Travel;
import ut.edu.project.services.BookingService;
import ut.edu.project.services.ReviewService;
import ut.edu.project.services.TravelService;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/travels") // Đường dẫn chính của trang du lịch
public class TravelController {

    private static final Logger log = LoggerFactory.getLogger(TravelController.class);

    @Autowired
    private TravelService travelService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public String showTravelList(Model model) {
        List<Travel> travels = travelService.getAllTravels(); // Lấy danh sách tour du lịch
        model.addAttribute("travels", travels);
        model.addAttribute("currentPage", "travels");
        return "travel/travel"; // Điều hướng đến file templates/travel/travel.html
    }

    // Method for travel details with reviews and booking status
    @GetMapping("/{id}")
    public String getTravelById(@PathVariable("id") Long id, Model model, Authentication authentication,
                              @Autowired(required = false) CsrfToken csrfToken) {
        try {
            Travel travel = travelService.getTravelById(id)
                    .orElseThrow(() -> new RuntimeException("Travel not found with id: " + id));

            // Get reviews for this travel
            List<Review> reviews = reviewService.getReviewsByTravel(id);
            Double averageRating = reviewService.getAverageRatingByTravel(id);
            Long reviewCount = reviewService.countReviewsByTravel(id);

            // Check if user has booked this travel and can review
            boolean hasBookedTravel = false;
            boolean hasReviewed = false;

            if (authentication != null) {
                String username = authentication.getName();
                hasBookedTravel = bookingService.hasUserBookedTravel(username, id);
                hasReviewed = reviewService.hasUserReviewedTravel(username, id);
            }

            model.addAttribute("travel", travel);
            model.addAttribute("reviews", reviews);
            model.addAttribute("averageRating", averageRating);
            model.addAttribute("reviewCount", reviewCount);
            model.addAttribute("hasBookedTravel", hasBookedTravel);
            model.addAttribute("hasReviewed", hasReviewed);
            model.addAttribute("currentPage", "travels");

            if (csrfToken != null) {
                model.addAttribute("_csrf", csrfToken);
            }

            return "travel/travel-detail";
        } catch (Exception e) {
            log.error("Error loading travel details: {}", e.getMessage(), e);
            return "redirect:/error?message=" + e.getMessage();
        }
    }
}
