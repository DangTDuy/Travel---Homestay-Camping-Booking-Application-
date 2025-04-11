package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.dtos.BookingRequestDTO;
import ut.edu.project.dtos.BookingResponseDTO;
import ut.edu.project.dtos.AdditionalDTO;
import ut.edu.project.models.Booking;
import ut.edu.project.models.Category;
import ut.edu.project.models.TimeSlot;
import ut.edu.project.models.Additional;
import ut.edu.project.services.BookingService;
import ut.edu.project.services.AdditionalService;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@RestController // Use RestController for API endpoints
@RequestMapping("/api") // Base path for all API endpoints in this controller
public class BookingApiController {

    private final BookingService bookingService;
    private final AdditionalService additionalService;

    @Autowired
    public BookingApiController(BookingService bookingService, AdditionalService additionalService) {
        this.bookingService = bookingService;
        this.additionalService = additionalService;
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
            // Log request data for debugging
            System.out.println("Booking request received: " + bookingRequest);
            if (bookingRequest.getAdditionalServices() != null) {
                System.out.println("Additional services count: " + bookingRequest.getAdditionalServices().size());
                for (AdditionalDTO service : bookingRequest.getAdditionalServices()) {
                    System.out.println("Service ID: " + service.getId() + ", Quantity: " + service.getQuantity());
                }
            }

            String username = authentication.getName();
            Booking createdBooking = bookingService.createBookingFromApi(bookingRequest, username);

            // Create and return the DTO instead of the full Booking entity
            BookingResponseDTO responseDto = new BookingResponseDTO(createdBooking);
            return ResponseEntity.ok(responseDto);

        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("Error creating booking: " + e.getMessage());
            // Log stack trace for better debugging of unexpected errors
            e.printStackTrace();
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
            }
            if (e.getMessage().contains("unavailable") || e.getMessage().contains("conflict")){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
            }
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
            // Sửa lỗi: boolean cannot be converted to java.util.List<ut.edu.project.models.Booking>
            // Sử dụng kết quả boolean trực tiếp từ bookingService.checkAvailability()
            boolean isAvailable = bookingService.checkAvailability(homestayId, checkIn, checkOut);
            return ResponseEntity.ok().body(Map.of("available", isAvailable));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error checking availability: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi kiểm tra tình trạng phòng: " + e.getMessage()));
        }
    }

    // Sửa lỗi: cannot find symbol method orElseGet(()->{ Addi[...]ce; })
    // Phương thức này có thể đã được thay đổi không đúng cách
    @GetMapping("/additional-services/{id}")
    public ResponseEntity<?> getAdditionalServiceById(@PathVariable Long id) {
        try {
            // AdditionalService.getAdditionalById trả về AdditionalDTO, không phải Optional
            AdditionalDTO service = additionalService.getAdditionalById(id);
            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Sửa lỗi: cannot find symbol method orElse(ut.edu.project.models.Category)
    @GetMapping("/categories/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        try {
            // AdditionalService.getCategoryById trả về Category, không phải Optional
            Category category = additionalService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Sửa lỗi: cannot find symbol method orElse(ut.edu.project.models.TimeSlot)
    @GetMapping("/timeslots/{id}")
    public ResponseEntity<?> getTimeSlotById(@PathVariable Long id) {
        try {
            // AdditionalService.getTimeSlotById trả về TimeSlot, không phải Optional
            TimeSlot timeSlot = additionalService.getTimeSlotById(id);
            return ResponseEntity.ok(timeSlot);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Add other potential API endpoints here (GET /api/bookings/my-bookings, etc.)
    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingByIdApi(@PathVariable Long id) {
        Optional<Booking> booking = bookingService.getBookingById(id);
        return booking.map(b -> ResponseEntity.ok(new BookingResponseDTO(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint for getting bookings by homestay ID
    @GetMapping("/homestays/{homestayId}/bookings")
    public ResponseEntity<?> getBookingsByHomestayId(@PathVariable Long homestayId) {
        try {
            List<Booking> bookings = bookingService.getBookingsByHomestayId(homestayId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Booking booking : bookings) {
                if (booking.getStatus() != Booking.BookingStatus.CANCELLED) {
                    Map<String, Object> bookingInfo = Map.of(
                        "id", booking.getId(),
                        "checkIn", booking.getCheckIn().toString(),
                        "checkOut", booking.getCheckOut().toString(),
                        "status", booking.getStatus().toString()
                    );
                    result.add(bookingInfo);
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi lấy danh sách đặt phòng: " + e.getMessage()));
        }
    }
}
