package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.dtos.TravelBookingDTO;
import ut.edu.project.models.Booking;
import ut.edu.project.models.Travel;
import ut.edu.project.models.User;
import ut.edu.project.services.BookingService;
import ut.edu.project.services.TravelService;
import ut.edu.project.services.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/travel-bookings")
public class TravelBookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TravelService travelService;

    @Autowired
    private UserService userService;

    @GetMapping("/{travelId}")
    public String showBookingForm(@PathVariable Long travelId, Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/auth/login-user";
        }

        Travel travel = travelService.getTravelById(travelId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour du lịch"));

        model.addAttribute("travel", travel);
        model.addAttribute("currentPage", "travels");
        return "booking/travel-booking-form";
    }

    @PostMapping("/api/book")
    @ResponseBody
    public ResponseEntity<?> bookTravel(@RequestBody TravelBookingDTO bookingDTO, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Bạn cần đăng nhập để đặt tour"));
            }

            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

            Travel travel = travelService.getTravelById(bookingDTO.getTravelId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tour du lịch"));

            // Kiểm tra số lượng người tham gia
            if (bookingDTO.getNumberOfPeople() > travel.getMaxParticipants()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Số lượng người tham gia vượt quá giới hạn"));
            }

            // Tạo booking mới
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setTravel(travel);
            booking.setServiceType(Booking.ServiceType.TRAVEL);
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setGuests(bookingDTO.getNumberOfPeople());
            booking.setCheckIn(bookingDTO.getStartDate());
            booking.setCheckOut(bookingDTO.getStartDate().plusDays(travel.getDurationDays()));
            booking.setTotalPrice(travel.getPrice() * bookingDTO.getNumberOfPeople());
            booking.setCreatedAt(LocalDateTime.now());
            booking.setIsReviewed(false);

            Booking savedBooking = bookingService.createBooking(booking);

            // Tăng số lượng đặt tour
            travel.setBookingCount(travel.getBookingCount() + 1);
            travelService.saveTravel(travel);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đặt tour thành công");
            response.put("bookingId", savedBooking.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
