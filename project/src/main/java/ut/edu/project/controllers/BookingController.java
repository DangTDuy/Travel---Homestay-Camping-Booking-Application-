package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Booking;
import ut.edu.project.services.BookingService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /* ==================== */
    /* == API ENDPOINTS == */
    /* ==================== */
    @ResponseBody
    @PostMapping("/api/create")
    public ResponseEntity<Booking> createBookingApi(@RequestBody Booking booking,
                                                    @RequestParam String paymentMethod) {
        try {
            Booking newBooking = bookingService.saveBooking(booking, paymentMethod);
            return ResponseEntity.ok(newBooking);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @ResponseBody
    @GetMapping("/api/{id}")
    public ResponseEntity<Booking> getBookingByIdApi(@PathVariable Long id) {
        Optional<Booking> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ResponseBody
    @GetMapping("/api")
    public ResponseEntity<List<Booking>> getAllBookingsApi() {
        return ResponseEntity.ok(bookingService.getAllBookings());
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
    public String showBookingDetails(@PathVariable Long id, Model model) {
        try {
            Optional<Booking> booking = bookingService.getBookingById(id);
            if (booking.isPresent()) {
                model.addAttribute("booking", booking.get());
                return "booking-details";
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
}