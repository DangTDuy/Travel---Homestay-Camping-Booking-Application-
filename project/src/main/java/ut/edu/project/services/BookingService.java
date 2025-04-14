package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import ut.edu.project.models.*;
import ut.edu.project.repositories.BookingRepository;
import ut.edu.project.repositories.PaymentRepository;
import ut.edu.project.repositories.UserRepository;
import ut.edu.project.repositories.HomestayRepository;
import ut.edu.project.repositories.AdditionalRepository;
import ut.edu.project.repositories.BookingAdditionalRepository;
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
import java.math.BigDecimal;

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

        // Lưu booking
        Booking savedBooking = bookingRepository.save(booking);

        // Lưu các BookingAdditional nếu có
        // Lưu ý: đây chỉ là danh sách hiển thị, không phải danh sách để lưu vào database
        // Vì vậy, chúng ta không cần lưu các BookingAdditional ở đây

        return savedBooking;
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

    @Transactional
    public Optional<Booking> getBookingById(Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);

        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();

            // Log thông tin về booking
            System.out.println("Booking ID: " + booking.getId());

            // Lấy thông tin dịch vụ bổ sung từ bảng BookingAdditional
            List<BookingAdditional> bookingAdditionals = bookingAdditionalRepository.findByBookingId(booking.getId());
            System.out.println("BookingAdditional links from DB: " + bookingAdditionals.size());

            // Tạo danh sách dịch vụ bổ sung từ các liên kết
            List<Additional> additionalServices = new ArrayList<>();
            for (BookingAdditional link : bookingAdditionals) {
                Additional service = link.getAdditional();
                if (service != null) {
                    // Khởi tạo các đối tượng liên quan
                    if (service.getCategory() != null) {
                        Hibernate.initialize(service.getCategory());
                    }
                    if (service.getTimeSlot() != null) {
                        Hibernate.initialize(service.getTimeSlot());
                    }

                    // Thiết lập số lượng từ BookingAdditional
                    service.setQuantity(link.getQuantity());
                    // Thiết lập giá từ BookingAdditional
                    service.setPrice(link.getPrice());

                    additionalServices.add(service);
                    System.out.println("Added service: " + service.getName() + ", Quantity: " + link.getQuantity());
                }
            }

            // Cập nhật danh sách dịch vụ bổ sung cho booking
            booking.setAdditionalServices(additionalServices);

            return Optional.of(booking);
        }

        return bookingOpt;
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

    @Autowired
    private BookingAdditionalRepository bookingAdditionalRepository;

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
            throw new RuntimeException("Homestay đã được đặt trong khoảng thời gian này. Vui lòng chọn ngày khác."); // Or HttpStatus.CONFLICT in Controller
        }

        // 5. Calculate Total Price
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nights <= 0) nights = 1; // Minimum 1 night stay
        double homestaySubtotal = homestay.getPrice() * nights;

        // Khởi tạo tổng giá ban đầu với giá homestay
        double serviceSubtotal = homestaySubtotal;

        // 6. Create and Save Booking
        Booking newBooking = new Booking();
        newBooking.setUser(user);
        newBooking.setHomestay(homestay);
        newBooking.setCheckIn(checkInDateTime);
        newBooking.setCheckOut(checkOutDateTime);
        newBooking.setGuests(request.getGuests());
        newBooking.setSpecialRequests(request.getSpecialRequests());

        // Thiết lập các trường bắt buộc ngay từ đầu
        newBooking.setServiceType(Booking.ServiceType.HOMESTAY); // Thiết lập loại dịch vụ ngay từ đầu
        newBooking.setStatus(Booking.BookingStatus.PENDING); // Trạng thái ban đầu
        newBooking.setCreatedAt(LocalDateTime.now());
        newBooking.setIsReviewed(false); // Ban đầu chưa được đánh giá
        newBooking.setTotalPrice(homestaySubtotal); // Thiết lập giá trị ban đầu cho totalPrice

        // 7. Process Additional Services if available
        double foodServicesTotal = 0.0;  // Tổng giá dịch vụ đồ ăn
        double otherServicesTotal = 0.0;  // Tổng giá các dịch vụ khác
        List<AdditionalDTO> selectedAdditionals = new ArrayList<>();

        if (request.getAdditionalServices() != null && !request.getAdditionalServices().isEmpty()) {
            for (AdditionalDTO additionalDTO : request.getAdditionalServices()) {
                Additional originalAdditional = additionalRepository.findById(additionalDTO.getId())
                        .orElseThrow(() -> new RuntimeException("Dịch vụ bổ sung không tồn tại với ID: " + additionalDTO.getId()));

                // Validate that additional service is available for this homestay
                // Chấp nhận cả dịch vụ chung (homestay = null) và dịch vụ riêng của homestay này
                if (originalAdditional.getHomestay() != null && !originalAdditional.getHomestay().getId().equals(homestay.getId())) {
                    throw new IllegalArgumentException("Dịch vụ bổ sung không thuộc về homestay đã chọn");
                }

                // Tính giá dựa trên loại dịch vụ
                double servicePrice = originalAdditional.getPrice().doubleValue();
                int quantity = additionalDTO.getQuantity();

                // Nếu là bữa ăn, tính theo số người
                boolean isFood = originalAdditional.getCategory() != null && "food".equals(originalAdditional.getCategory().getName());
                if (isFood) {
                    servicePrice = servicePrice * request.getGuests();
                }

                // Nếu là gói dịch vụ, tính theo số ngày
                if (originalAdditional.getCategory() != null && "service".equals(originalAdditional.getCategory().getName())) {
                    servicePrice = servicePrice * nights;
                }

                // Tính tổng giá dịch vụ theo loại
                double serviceTotalPrice = servicePrice * quantity;
                if (isFood) {
                    foodServicesTotal += serviceTotalPrice;
                } else {
                    otherServicesTotal += serviceTotalPrice;
                }

                // Lưu thông tin dịch vụ để xử lý sau khi lưu booking
                additionalDTO.setPrice(new BigDecimal(servicePrice));
                selectedAdditionals.add(additionalDTO);

                // Thêm dịch vụ vào danh sách hiển thị
                newBooking.addAdditionalService(originalAdditional);
            }
        }

        // Cộng thêm giá dịch vụ đồ ăn và dịch vụ khác vào tổng giá trị dịch vụ
        serviceSubtotal += foodServicesTotal + otherServicesTotal;

        // Tính phí dịch vụ 20% cho tất cả dịch vụ
        double serviceFee = serviceSubtotal * 0.2;

        // Tổng giá = tổng giá trị dịch vụ + phí dịch vụ 20%
        double totalPrice = serviceSubtotal + serviceFee;

        // Cập nhật tổng giá cuối cùng
        newBooking.setTotalPrice(totalPrice);

        // Lưu booking trước để có ID
        newBooking = bookingRepository.save(newBooking);

        // Bây giờ mới tạo và lưu các BookingAdditional
        if (!selectedAdditionals.isEmpty()) {
            for (AdditionalDTO additionalDTO : selectedAdditionals) {
                // Tạo và lưu BookingAdditional
                BookingAdditional bookingAdditional = new BookingAdditional();
                Additional additional = additionalRepository.findById(additionalDTO.getId()).orElseThrow();
                bookingAdditional.setBooking(newBooking);
                bookingAdditional.setAdditional(additional);
                bookingAdditional.setQuantity(additionalDTO.getQuantity());
                bookingAdditional.setPrice(additionalDTO.getPrice());
                bookingAdditional.setServiceDate(newBooking.getCheckIn()); // Sử dụng ngày nhận phòng làm ngày dịch vụ
                bookingAdditional.setTimeSlot(additional.getTimeSlot()); // Thiết lập timeSlot từ additional

                // Tính tổng giá = giá * số lượng
                BigDecimal itemTotalPrice = additionalDTO.getPrice().multiply(new BigDecimal(additionalDTO.getQuantity()));
                bookingAdditional.setTotalPrice(itemTotalPrice);
                bookingAdditionalRepository.save(bookingAdditional);

                System.out.println("Created booking-additional link: Booking ID=" + newBooking.getId() +
                        ", Additional ID=" + additionalDTO.getId() +
                        ", Quantity=" + additionalDTO.getQuantity());
            }
        }

        return newBooking;
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

        // Calculate total for additional services using BookingAdditional
        double additionalServicesTotal = 0;
        List<BookingAdditional> bookingAdditionals = bookingAdditionalRepository.findByBookingId(booking.getId());
        if (bookingAdditionals != null && !bookingAdditionals.isEmpty()) {
            for (BookingAdditional link : bookingAdditionals) {
                additionalServicesTotal += link.getPrice().doubleValue() * link.getQuantity();
            }
        }

        // Tính tổng giá trị dịch vụ (homestay + dịch vụ bổ sung)
        double serviceSubtotal = subtotal + additionalServicesTotal;

        // Tính phí dịch vụ 20% cho tất cả dịch vụ
        double serviceFee = serviceSubtotal * 0.2;

        // Total price = tổng giá trị dịch vụ + phí dịch vụ 20%
        double totalPrice = serviceSubtotal + serviceFee;

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

    /**
     * Kiểm tra xem người dùng đã đặt tour này chưa
     * @param username Tên người dùng
     * @param travelId ID của tour
     * @return true nếu người dùng đã đặt tour này, false nếu chưa
     */
    public boolean hasUserBookedTravel(String username, Long travelId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        // Đếm số lượng booking đã hoàn thành của user đối với travel này
        Long count = bookingRepository.countByUserIdAndTravelIdAndStatus(
                user.getId(), travelId, Booking.BookingStatus.COMPLETED.toString());

        return count != null && count > 0;
    }

    /**
     * Lấy danh sách booking theo homestay ID
     * @param homestayId ID của homestay
     * @return Danh sách booking của homestay
     */
    public List<Booking> getBookingsByHomestayId(Long homestayId) {
        return bookingRepository.findByHomestayId(homestayId);
    }
}