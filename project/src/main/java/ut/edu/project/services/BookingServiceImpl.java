package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Booking;
import ut.edu.project.models.Payment;
import ut.edu.project.models.User;
import ut.edu.project.repositories.BookingRepository;
import ut.edu.project.services.BookingService;
import ut.edu.project.services.PaymentService;
import ut.edu.project.services.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    @Override
    public Booking saveBooking(Booking booking, String paymentMethod) {
        // Lưu booking
        booking.setStatus("Đã đặt");
        Booking savedBooking = bookingRepository.save(booking);

        // Tạo payment tương ứng
        Payment payment = new Payment();
        payment.setBooking(savedBooking);
        payment.setPayer(booking.getUser());
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("Chưa thanh toán");
        payment.setPaymentDate(LocalDateTime.now());
        paymentService.savePayment(payment);

        // Liên kết payment với booking
        savedBooking.setPayment(payment);
        return bookingRepository.save(savedBooking);
    }

    @Override
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public Booking updateBookingStatus(Long id, String status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(status);
            return bookingRepository.save(booking);
        }
        throw new RuntimeException("Booking not found");
    }

    @Override
    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }
}