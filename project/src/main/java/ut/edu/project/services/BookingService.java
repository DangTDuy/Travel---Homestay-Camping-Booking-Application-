package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Booking;
import ut.edu.project.models.Payment;
import ut.edu.project.repositories.BookingRepository;
import ut.edu.project.repositories.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

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

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    public Booking updateBookingStatus(Long id, String status) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            booking.setStatus(status);
            return bookingRepository.save(booking);
        } else {
            return null; // or throw an exception
        }
    }

    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    // Additional methods based on requirements can be added here.
    public List<Booking> getBookingsByStatus(String status){
        return bookingRepository.findByStatus(status);
    }
}