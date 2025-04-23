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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public String showBookingDetails(@PathVariable("id") Long id, Model model, Principal principal) {
        try {
            Optional<Booking> bookingOpt = bookingService.getBookingById(id);
            if (bookingOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy đặt phòng với ID: " + id);
                return "redirect:/bookings/my-history";
            }

            Booking booking = bookingOpt.get();

            // Kiểm tra quyền truy cập
            if (principal == null) {
                return "redirect:/auth/login-user";
            }

            User user = userService.findByUsername(principal.getName()).orElse(null);
            if (user == null) {
                return "redirect:/auth/login-user";
            }

            // Kiểm tra nếu người dùng là chủ sở hữu đặt phòng hoặc là admin
            boolean isOwner = booking.getUser().getUsername().equals(principal.getName());
            boolean isAdmin = "ADMIN".equals(user.getRole());

            if (!isOwner && !isAdmin) {
                return "redirect:/bookings/my-history";
            }

            // In thông tin dịch vụ bổ sung để debug
            System.out.println("Booking ID: " + booking.getId());
            System.out.println("Additional Services Size: " + booking.getAdditionalServices().size());
            for (Additional service : booking.getAdditionalServices()) {
                System.out.println("Service: " + service.getName() + ", Category: " +
                        (service.getCategory() != null ? service.getCategory().getName() : "null") +
                        ", TimeSlot: " + (service.getTimeSlot() != null ? service.getTimeSlot().getName() : "null"));
            }

            model.addAttribute("booking", booking);
            model.addAttribute("currentPage", "bookings");

            if (isAdmin) {
                model.addAttribute("allStatuses", Booking.BookingStatus.values());
                return "admin/booking-detail";
            } else {
                return "user/booking-detail";
            }
        } catch (Exception e) {
            e.printStackTrace(); // In ra lỗi chi tiết để debug
            model.addAttribute("error", "Lỗi khi tải thông tin đặt phòng: " + e.getMessage());
            return "redirect:/bookings/my-history";
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
            model.addAttribute("currentPage", "bookings");
            return "user/booking-history";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải lịch sử đặt phòng: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/homestay/{homestayId}/book")
    public String showHomestayBookingForm(@PathVariable("homestayId") Long homestayId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login-user";
        }

        Optional<Homestay> homestay = homestayService.getHomestayById(homestayId);
        if (homestay.isEmpty()) {
            return "redirect:/homestay";
        }

        // Lấy dịch vụ bổ sung cho homestay này (bao gồm cả dịch vụ chung)
        List<Additional> additionals = additionalService.getByHomestay(homestay.get());
        model.addAttribute("homestay", homestay.get());
        model.addAttribute("additionals", additionals);
        model.addAttribute("booking", new Booking());
        model.addAttribute("currentPage", "bookings");
        return "booking/homestay-booking-form";
    }

    @PostMapping("/homestay/{homestayId}/book")
    public String createHomestayBooking(
            @PathVariable("homestayId") Long homestayId,
            @Valid @ModelAttribute Booking booking,
            BindingResult result,
            @RequestParam(name = "additionalIds", required = false) List<Long> additionalIds,
            @RequestParam(name = "checkInTime", required = false) String checkInTime,
            @RequestParam(name = "checkOutTime", required = false) String checkOutTime,
            @RequestParam(required = false, name = "quantity-") Map<String, String> quantities,
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

        // Xử lý thời gian check-in và check-out
        if (checkInTime != null && !checkInTime.isEmpty()) {
            // Lấy ngày từ booking.getCheckIn() và thêm giờ từ checkInTime
            LocalDate checkInDate = booking.getCheckIn().toLocalDate();
            LocalTime time = LocalTime.parse(checkInTime);
            booking.setCheckIn(LocalDateTime.of(checkInDate, time));
        }

        if (checkOutTime != null && !checkOutTime.isEmpty()) {
            // Lấy ngày từ booking.getCheckOut() và thêm giờ từ checkOutTime
            LocalDate checkOutDate = booking.getCheckOut().toLocalDate();
            LocalTime time = LocalTime.parse(checkOutTime);
            booking.setCheckOut(LocalDateTime.of(checkOutDate, time));
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

            // Add additional services to display list only
            if (additionalIds != null && !additionalIds.isEmpty()) {
                List<Additional> additionals = additionalService.getByIds(additionalIds);
                for (Additional additional : additionals) {
                    // Lấy số lượng từ form
                    int quantity = 1; // Mặc định là 1
                    String quantityKey = "quantity-" + additional.getId();
                    if (quantities.containsKey(quantityKey)) {
                        try {
                            quantity = Integer.parseInt(quantities.get(quantityKey));
                            if (quantity < 1) quantity = 1;
                        } catch (NumberFormatException e) {
                            // Giữ mặc định là 1 nếu có lỗi
                        }
                    }

                    // Thiết lập số lượng cho dịch vụ
                    additional.setQuantity(quantity);
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
    public String cancelBooking(@PathVariable("id") Long id, Principal principal, Model model) {
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
    public String showPaymentPage(@PathVariable("id") Long id, Model model, Principal principal) {
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