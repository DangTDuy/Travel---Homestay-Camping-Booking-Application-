package ut.edu.project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.*;
import ut.edu.project.services.*;
import ut.edu.project.dtos.BookingRequestDTO;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final HomestayService homestayService;
    private final CampingService campingService;
    private final AdditionalService additionalService;

    @Autowired
    public BookingController(
            BookingService bookingService,
            UserService userService,
            HomestayService homestayService,
            CampingService campingService,
            AdditionalService additionalService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.homestayService = homestayService;
        this.campingService = campingService;
        this.additionalService = additionalService;
    }

    /* ==================== */
    /* == VIEW ENDPOINTS == */
    /* ==================== */
    @GetMapping
    public String showAllBookings(Model model) {
        try {
            model.addAttribute("bookings", bookingService.getAllBookings());
            return "bookings";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load bookings");
            return "error";
        }
    }

    @GetMapping("/{id}")
    public String showBookingDetails(@PathVariable Long id, Model model, Principal principal) {
        try {
            Optional<Booking> booking = bookingService.getBookingById(id);
            if (booking.isPresent()) {
                if (principal != null && booking.get().getUser().getUsername().equals(principal.getName())) {
                    model.addAttribute("booking", booking.get());
                    User user = userService.findByUsername(principal.getName()).orElse(null);
                    if (user != null && "ADMIN".equals(user.getRole())) {
                        model.addAttribute("allStatuses", Booking.BookingStatus.values());
                        return "admin/booking-detail";
                    } else {
                        return "user/booking-detail";
                    }
                } else {
                    return "redirect:/bookings/my-bookings";
                }
            }
            return "redirect:/bookings";
        } catch (Exception e) {
            model.addAttribute("error", "Booking not found");
            return "error";
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("booking", new Booking());
        return "create-booking";
    }

    // === User Booking Management ===
    @GetMapping("/my-bookings")
    public String showMyBookings(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login-user";
        }
        String username = principal.getName();
        List<Booking> bookings = bookingService.getTop3BookingsByUsername(username);
        model.addAttribute("bookings", bookings);
        model.addAttribute("isFullHistory", false);
        return "user/user-bookings";
    }

    @GetMapping("/my-history")
    public String showMyBookingHistory(
            Model model, 
            Principal principal,
            @RequestParam(defaultValue = "0") int page) {
        if (principal == null) {
            return "redirect:/auth/login-user";
        }
        try {
            String username = principal.getName();
            // Get bookings with pagination (20 records per page)
            List<Booking> bookings = bookingService.getBookingsByUsername(username, page, 20);
            model.addAttribute("bookings", bookings);
            model.addAttribute("isFullHistory", true);
            model.addAttribute("username", username);
            model.addAttribute("page", page);
            return "user/booking-history";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải lịch sử đặt phòng: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/homestay/{homestayId}/book")
    public String showHomestayBookingForm(@PathVariable Long homestayId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login-user";
        }

        Optional<Homestay> homestay = homestayService.getHomestayById(homestayId);
        if (homestay.isEmpty()) {
            return "redirect:/homestay";
        }

        List<Additional> additionals = additionalService.getByHomestay(homestay.get());
        model.addAttribute("homestay", homestay.get());
        model.addAttribute("additionals", additionals);
        model.addAttribute("booking", new Booking());
        return "booking/homestay-booking-form";
    }

    @PostMapping("/homestay/{homestayId}/book")
    public String createHomestayBooking(
            @PathVariable Long homestayId,
            @Valid @ModelAttribute Booking booking,
            BindingResult result,
            @RequestParam(required = false) List<Long> additionalIds,
            Principal principal,
            Model model) {

        if (result.hasErrors()) {
            Optional<Homestay> homestay = homestayService.getHomestayById(homestayId);
            if (homestay.isPresent()) {
                model.addAttribute("homestay", homestay.get());
                model.addAttribute("additionals", additionalService.getByHomestay(homestay.get()));
            }
            return "booking/homestay-booking-form";
        }

        try {
            String username = principal.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Homestay homestay = homestayService.getHomestayById(homestayId)
                    .orElseThrow(() -> new RuntimeException("Homestay not found"));

            booking.setUser(user);
            booking.setHomestay(homestay);
            booking.setServiceType(Booking.ServiceType.HOMESTAY);

            // Add additional services if selected
            if (additionalIds != null && !additionalIds.isEmpty()) {
                List<Additional> additionals = additionalService.getByIds(additionalIds);
                for (Additional additional : additionals) {
                    booking.addAdditionalService(additional);
                }
            }

            Booking savedBooking = bookingService.createBooking(booking);
            return "redirect:/bookings/" + savedBooking.getId() + "/payment";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "booking/homestay-booking-form";
        }
    }

    @GetMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, Principal principal, Model model) {
        String username = principal.getName(); // Get username early
        try {
            bookingService.cancelBooking(id, username);
            // Redirect back to the full history page after successful cancellation
            return "redirect:/bookings/my-history";
        } catch (RuntimeException e) { // Catch specific runtime exceptions from service
            // Add error message to flash attributes to show after redirect
            // Assuming you have RedirectAttributes injected or configure flash scope
            // For now, just redirecting back, error message might be lost
            // Ideally, use RedirectAttributes: redirectAttributes.addFlashAttribute("error", e.getMessage());
            model.addAttribute("error", e.getMessage()); // This won't show after redirect
            System.err.println("Error cancelling booking: " + e.getMessage()); // Log error
            // Redirect back to the full history page even if there's an error
            return "redirect:/bookings/my-history?error=true"; // Add param to potentially show error message
        }
    }

    @GetMapping("/{id}/payment")
    public String showPaymentPage(@PathVariable Long id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login-user";
        }

        try {
            Booking booking = bookingService.getBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (!booking.getUser().getUsername().equals(principal.getName())) {
                return "redirect:/bookings/my-bookings";
            }

            model.addAttribute("booking", booking);
            return "booking/payment";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/bookings/my-bookings";
        }
    }

    // === Admin Booking Management ===
    @GetMapping("/admin")
    public String showAdminBookings(
            @RequestParam(required = false) Booking.BookingStatus status,
            @RequestParam(required = false) String searchTerm,
            Model model) {
        List<Booking> bookings;
        if (status != null) {
            bookings = bookingService.getBookingsByStatus(status);
        } else if (searchTerm != null && !searchTerm.isEmpty()) {
            bookings = bookingService.searchBookings(searchTerm);
        } else {
            bookings = bookingService.getAllBookings();
        }
        model.addAttribute("bookings", bookings);
        model.addAttribute("statuses", Booking.BookingStatus.values());
        return "admin/admin-bookings";
    }

    // REMOVED: This endpoint is now in AdminBookingController
    // @PostMapping("/admin/{id}/status")
    // public ResponseEntity<?> updateBookingStatus(
    //         @PathVariable Long id,
    //         @RequestParam Booking.BookingStatus status,
    //         Authentication authentication) {
    //     try {
    //         String username = authentication.getName();
    //         Booking updated = bookingService.updateBookingStatus(id, status, username);
    //         return ResponseEntity.ok(updated);
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body(e.getMessage());
    //     }
    // }
}