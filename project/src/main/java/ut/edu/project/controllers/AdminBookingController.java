package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ut.edu.project.models.Booking;
import ut.edu.project.services.BookingService;
import ut.edu.project.services.UserService;

@Controller
@RequestMapping("/admin/bookings")
@PreAuthorize("hasAuthority('ADMIN')") // Use hasAuthority to check for exact role name 'ADMIN'
public class AdminBookingController {

    private final BookingService bookingService;
    private final UserService userService; // Inject if needed for user details

    @Autowired
    public AdminBookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    // Endpoint to show all bookings for admin with pagination and filtering
    @GetMapping
    public String showAllBookings(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String serviceType,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        // Convert string parameters to enum if needed
        Booking.BookingStatus statusEnum = null;
        if (!status.isEmpty()) {
            try {
                statusEnum = Booking.BookingStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }
        
        // Get paginated bookings with filters
        Page<Booking> bookingsPage = bookingService.getAdminBookings(statusEnum, serviceType, dateFrom, dateTo, page, size);
        
        model.addAttribute("bookings", bookingsPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalItems", bookingsPage.getTotalElements());
        model.addAttribute("totalPages", bookingsPage.getTotalPages());
        model.addAttribute("hasNext", bookingsPage.hasNext());
        model.addAttribute("hasPrevious", bookingsPage.hasPrevious());
        
        // Add selected filters to model for form restoration
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedServiceType", serviceType);
        model.addAttribute("selectedDateFrom", dateFrom);
        model.addAttribute("selectedDateTo", dateTo);
        
        // Add isAdmin attribute for the template
        model.addAttribute("isAdmin", true);
        model.addAttribute("currentPage", "admin/bookings");
        
        return "admin/bookings";
    }
    
    // Endpoint to show the booking detail page for Admin
    @GetMapping("/{id}")
    public String showAdminBookingDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.getBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

            model.addAttribute("booking", booking);
            model.addAttribute("allStatuses", Booking.BookingStatus.values()); // For the status update dropdown
            model.addAttribute("currentPage", "admin/bookings");
            return "admin/booking-detail"; // Path to the new template

        } catch (RuntimeException e) {
            // Log the error (optional)
            System.err.println("Error fetching booking for admin view: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đặt phòng với ID: " + id);
            return "redirect:/dashboard"; // Redirect back to admin dashboard on error
        }
    }

    // Endpoint to handle status update from the detail page
    @PostMapping("/{id}/status")
    public String updateBookingStatusAdmin(
            @PathVariable Long id,
            @RequestParam Booking.BookingStatus status,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName(); // Admin username
            bookingService.updateBookingStatus(id, status, username);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái đặt phòng thành công!");
        } catch (RuntimeException e) {
            System.err.println("Error updating booking status: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/bookings/" + id; // Redirect back to the detail page
    }

    // Add other admin-specific booking actions here if needed (e.g., delete)

} 