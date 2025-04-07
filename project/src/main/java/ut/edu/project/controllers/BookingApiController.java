package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.dtos.BookingRequestDTO;
import ut.edu.project.dtos.BookingResponseDTO;
import ut.edu.project.models.Booking;
import ut.edu.project.services.BookingService;

import java.util.Map;

@RestController // Use RestController for API endpoints
@RequestMapping("/api") // Base path for all API endpoints in this controller
public class BookingApiController {

    private final BookingService bookingService;

    @Autowired
    public BookingApiController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Endpoint for handling booking from homestay detail modal
    @PostMapping("/bookings") // Maps to POST /api/bookings
    public ResponseEntity<?> createHomestayBookingApi(@RequestBody BookingRequestDTO bookingRequest,
                                                      Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "Vui lòng đăng nhập để đặt phòng."));
        }

        try {
            String username = authentication.getName();
            Booking createdBooking = bookingService.createBookingFromApi(bookingRequest, username);

            // Create and return the DTO instead of the full Booking entity
            BookingResponseDTO responseDto = new BookingResponseDTO(createdBooking);
            return ResponseEntity.ok(responseDto);

        } catch (IllegalArgumentException e) {
             return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
             System.err.println("Error creating booking: " + e.getMessage());
             // Log stack trace for better debugging of unexpected errors
             // e.printStackTrace();
             if (e.getMessage().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
             }
             if (e.getMessage().contains("unavailable") || e.getMessage().contains("conflict")){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
             }
             // Catch potential StackOverflowError from circular references if DTO wasn't used
             // (Should not happen now, but good practice to be aware of)
             // } catch (StackOverflowError soe) { 
             //    System.err.println("StackOverflowError during booking creation/serialization: " + soe.getMessage());
             //    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
             //                         .body(Map.of("message", "Lỗi hệ thống: Vấn đề tham chiếu vòng tròn khi xử lý dữ liệu."));
             // }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("message", "Lỗi hệ thống khi tạo đặt phòng: " + e.getMessage()));
        }
    }

    // Endpoint for checking availability
    @GetMapping("/bookings/check-availability") // Maps to GET /api/bookings/check-availability
    public ResponseEntity<?> checkAvailability(
            @RequestParam Long homestayId,
            @RequestParam String checkIn, // Expecting yyyy-MM-dd format from potential future JS calls
            @RequestParam String checkOut) {
        try {
            // Consider adding date parsing/validation here or in service if needed
            boolean isAvailable = bookingService.checkAvailability(homestayId, checkIn, checkOut);
            return ResponseEntity.ok().body(new AvailabilityResponse(isAvailable));
        } catch (IllegalArgumentException e) {
             return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
             System.err.println("Error checking availability: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("message", "Lỗi khi kiểm tra tình trạng phòng: " + e.getMessage()));
        }
    }

    // Inner class for Availability Response
    private static class AvailabilityResponse {
        private final boolean available;

        public AvailabilityResponse(boolean available) {
            this.available = available;
        }

        public boolean isAvailable() {
            return available;
        }
    }

    // Add other potential API endpoints here (GET /api/bookings/{id}, GET /api/bookings/my-bookings, etc.)
    // Example:
    /*
    @GetMapping("/bookings/{id}")
    public ResponseEntity<Booking> getBookingByIdApi(@PathVariable Long id) {
        Optional<Booking> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    */
} 