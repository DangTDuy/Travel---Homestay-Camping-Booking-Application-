package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.project.models.*;
import ut.edu.project.repositories.BookingRepository;
import ut.edu.project.repositories.PaymentRepository;
import ut.edu.project.repositories.UserRepository;
import ut.edu.project.repositories.HomestayRepository;
import ut.edu.project.repositories.AdditionalRepository;
import ut.edu.project.dtos.BookingRequestDTO;
import ut.edu.project.dtos.AdditionalDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private AdditionalRepository additionalRepository;

    @Transactional
    public Booking createBooking(Booking booking) {
        // Validate booking
        validateBooking(booking);

        // Calculate total price including additional services
        calculateTotalPrice(booking);

        // Set initial status
        booking.setStatus(Booking.BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking saveBooking(Booking booking, String paymentMethod) {
        Booking savedBooking = bookingRepository.save(booking);

        // Create Payment
        Payment payment = new Payment();
        payment.setBooking(savedBooking);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(savedBooking.getTotalPrice());
        paymentRepository.save(payment);

        return savedBooking;
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUsername(String username, int page, int size) {
        int offset = page * size;
        return bookingRepository.findByUserUsernameOrderByCreatedAtDesc(username, offset, size);
    }

    public List<Booking> getTop3BookingsByUsername(String username) {
        return bookingRepository.findTop3ByUserUsernameOrderByCreatedAtDesc(username);
    }

    public Page<Booking> getAdminBookings(Booking.BookingStatus status, String serviceType, String dateFrom, String dateTo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Xử lý các tham số lọc
        Booking.ServiceType serviceTypeEnum = null;
        if (serviceType != null && !serviceType.isEmpty()) {
            try {
                serviceTypeEnum = Booking.ServiceType.valueOf(serviceType);
            } catch (IllegalArgumentException e) {
                // Bỏ qua nếu serviceType không hợp lệ
            }
        }

        // Xử lý ngày tháng
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        if (dateFrom != null && !dateFrom.isEmpty()) {
            try {
                fromDate = LocalDate.parse(dateFrom).atStartOfDay();
            } catch (Exception e) {
                // Bỏ qua nếu định dạng ngày không hợp lệ
            }
        }

        if (dateTo != null && !dateTo.isEmpty()) {
            try {
                toDate = LocalDate.parse(dateTo).atTime(LocalTime.MAX);
            } catch (Exception e) {
                // Bỏ qua nếu định dạng ngày không hợp lệ
            }
        }

        return bookingRepository.findBookingsWithFilters(status, serviceTypeEnum, fromDate, toDate, pageable);
    }

    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        // Call the updated repository method with sorting
        return bookingRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Booking> searchBookings(String searchTerm) {
        // Call the updated repository method that includes Camping, Travel (tourName), and sorting
        return bookingRepository.findByUserUsernameContainingIgnoreCaseOrHomestayNameContainingIgnoreCaseOrCampingNameContainingIgnoreCaseOrTravelTourNameContainingIgnoreCaseOrderByCreatedAtDesc(searchTerm);
    }

    @Transactional
    public void cancelBooking(Long id, String username) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You don't have permission to cancel this booking");
        }

        if (!booking.canCancel()) {
            throw new RuntimeException("This booking cannot be cancelled");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateBookingStatus(Long id, Booking.BookingStatus status, String username) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().equals("ADMIN")) {
            throw new RuntimeException("Only admin can update booking status");
        }

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public boolean checkAvailability(Long homestayId, String checkIn, String checkOut) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime checkInDate = LocalDateTime.parse(checkIn, formatter);
        LocalDateTime checkOutDate = LocalDateTime.parse(checkOut, formatter);

        // Validate dates
        if (checkInDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }
        if (checkOutDate.isBefore(checkInDate)) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }

        // Check for overlapping bookings
        List<Booking> overlappingBookings = bookingRepository
                .findOverlappingBookings(homestayId, checkInDate, checkOutDate);

        return overlappingBookings.isEmpty();
    }

    @Transactional
    public Booking createBookingFromApi(BookingRequestDTO request, String username) {
        // 1. Fetch User and Homestay
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        Homestay homestay = homestayRepository.findById(request.getHomestayId())
                .orElseThrow(() -> new RuntimeException("Homestay not found with ID: " + request.getHomestayId()));

        // 2. Parse and Validate Dates
        LocalDate checkInDate;
        LocalDate checkOutDate;
        try {
            checkInDate = LocalDate.parse(request.getCheckInDate());
            checkOutDate = LocalDate.parse(request.getCheckOutDate());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ. Vui lòng sử dụng yyyy-MM-dd.");
        }

        if (checkInDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày nhận phòng không thể là ngày trong quá khứ.");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("Ngày trả phòng phải sau ngày nhận phòng.");
        }

        // Combine with default time (e.g., check-in at 14:00, check-out at 12:00)
        // Adjust these times as needed for your business logic
        LocalDateTime checkInDateTime = checkInDate.atTime(14, 0);
        LocalDateTime checkOutDateTime = checkOutDate.atTime(12, 0);

        // 3. Validate Guests
        if (request.getGuests() <= 0) {
            throw new IllegalArgumentException("Số lượng khách phải lớn hơn 0.");
        }
        if (request.getGuests() > homestay.getCapacity()) {
            throw new IllegalArgumentException("Số lượng khách vượt quá sức chứa của homestay (" + homestay.getCapacity() + ").");
        }

        // 4. Check Availability (Overlap)
        List<Booking> overlappingBookings = bookingRepository
                .findOverlappingBookings(homestay.getId(), checkInDateTime, checkOutDateTime);
        if (!overlappingBookings.isEmpty()) {
            throw new RuntimeException("Homestay không có sẵn trong khoảng thời gian đã chọn."); // Or HttpStatus.CONFLICT in Controller
        }

        // 5. Calculate Total Price
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nights <= 0) nights = 1; // Minimum 1 night stay
        double subtotal = homestay.getPrice() * nights;

        // Calculate service fee (20% of subtotal)
        double serviceFee = subtotal * 0.2;

        // Initialize total price with subtotal + service fee
        double totalPrice = subtotal + serviceFee;

        // 6. Create and Save Booking
        Booking newBooking = new Booking();
        newBooking.setUser(user);
        newBooking.setHomestay(homestay);
        newBooking.setCheckIn(checkInDateTime);
        newBooking.setCheckOut(checkOutDateTime);
        newBooking.setGuests(request.getGuests());
        newBooking.setSpecialRequests(request.getSpecialRequests());

        // 7. Process Additional Services if available
        if (request.getAdditionalServices() != null && !request.getAdditionalServices().isEmpty()) {
            List<Additional> additionalServices = new ArrayList<>();

            for (AdditionalDTO additionalDTO : request.getAdditionalServices()) {
                Additional additional = additionalRepository.findById(additionalDTO.getId())
                        .orElseThrow(() -> new RuntimeException("Dịch vụ bổ sung không tồn tại với ID: " + additionalDTO.getId()));

                // Set quantity from request
                additional.setQuantity(additionalDTO.getQuantity());

                // Validate that additional service belongs to the selected homestay
                if (!additional.getHomestay().getId().equals(homestay.getId())) {
                    throw new IllegalArgumentException("Dịch vụ bổ sung không thuộc về homestay đã chọn");
                }

                // Add the price of this service to the total
                totalPrice += additional.getPrice().doubleValue() * additional.getQuantity();

                additionalServices.add(additional);
            }

            newBooking.setAdditionalServices(additionalServices);
        }

        newBooking.setTotalPrice(totalPrice);
        newBooking.setStatus(Booking.BookingStatus.PENDING); // Initial status
        newBooking.setServiceType(Booking.ServiceType.HOMESTAY);
        newBooking.setCreatedAt(LocalDateTime.now());
        newBooking.setIsReviewed(false); // Initially not reviewed

        return bookingRepository.save(newBooking);
    }

    private void validateBooking(Booking booking) {
        if (booking.getCheckIn().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }
        if (booking.getCheckOut().isBefore(booking.getCheckIn())) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }
        if (booking.getGuests() <= 0) {
            throw new RuntimeException("Number of guests must be positive");
        }
    }

    private void calculateTotalPrice(Booking booking) {
        double basePrice = booking.getHomestay().getPrice(); // Base price per night

        // Calculate number of nights
        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                booking.getCheckIn().toLocalDate(),
                booking.getCheckOut().toLocalDate()
        );
        double subtotal = basePrice * nights;

        // Calculate service fee (20% of subtotal)
        double serviceFee = subtotal * 0.2;

        // Calculate total for additional services
        double additionalServicesTotal = 0;
        if (booking.getAdditionalServices() != null) {
            for (Additional service : booking.getAdditionalServices()) {
                additionalServicesTotal += service.getPrice().doubleValue() * service.getQuantity();
            }
        }

        // Total price = subtotal + service fee + additional services
        double totalPrice = subtotal + serviceFee + additionalServicesTotal;

        booking.setTotalPrice(totalPrice);
    }

    /**
     * Tìm booking đã hoàn thành và chưa được đánh giá cho một homestay
     * @param userId ID của người dùng
     * @param homestayId ID của homestay
     * @return Booking đã hoàn thành và chưa được đánh giá, hoặc null nếu không tìm thấy
     */
    public Booking findCompletedBookingForReview(Long userId, Long homestayId) {
        List<Booking> completedBookings = bookingRepository.findByUserIdAndHomestayIdAndStatusAndIsReviewedFalse(
                userId, homestayId, Booking.BookingStatus.COMPLETED);

        if (completedBookings != null && !completedBookings.isEmpty()) {
            return completedBookings.get(0); // Trả về booking đầu tiên tìm thấy
        }

        return null;
    }
}