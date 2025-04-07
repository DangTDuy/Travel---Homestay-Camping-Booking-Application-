package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl; // Thêm import cho PageImpl
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.project.models.Camping;
import ut.edu.project.models.User;
import ut.edu.project.models.Booking;
import ut.edu.project.repositories.CampingRepository;
import ut.edu.project.services.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Thêm import cho Collectors

@Service
public class CampingService {
    @Autowired
    private CampingRepository campingRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Camping createCamping(Camping camping) {
        validateCamping(camping);
        return campingRepository.save(camping);
    }

    @Transactional
    public Camping updateCamping(Long id, Camping camping) {
        Camping existingCamping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại"));

        // Cập nhật thông tin
        existingCamping.setName(camping.getName());
        existingCamping.setLocation(camping.getLocation());
        existingCamping.setDescription(camping.getDescription());
        existingCamping.setPrice(camping.getPrice());
        existingCamping.setMaxPlaces(camping.getMaxPlaces());
        existingCamping.setIsAvailable(camping.isAvailable());
        existingCamping.setImageUrls(camping.getImageUrls());
        existingCamping.setFacilities(camping.getFacilities());
        existingCamping.setRules(camping.getRules());
        existingCamping.setTerrain(camping.getTerrain());
        existingCamping.setWeather(camping.getWeather());
        existingCamping.setEquipment(camping.getEquipment());

        validateCamping(existingCamping);
        return campingRepository.save(existingCamping);
    }

    public Optional<Camping> getCampingById(Long id) {
        return campingRepository.findById(id);
    }

    public List<Camping> getAllCampings() {
        return campingRepository.findAll();
    }

    public List<Camping> searchCampings(
            String location,
            Double minPrice,
            Double maxPrice,
            Integer minPlaces,
            Boolean isAvailable) {
        return campingRepository.searchCampings(location, minPrice, maxPrice, minPlaces, isAvailable);
    }

    public List<Camping> getCampingsByOwner(User owner) {
        return campingRepository.findByOwner(owner);
    }

    public List<Camping> getAvailableCampings() {
        return campingRepository.findByIsAvailableTrue();
    }

    public List<Camping> getCampingsByFacilities(List<String> facilities) {
        return campingRepository.findByFacilitiesIn(facilities);
    }

    public List<Camping> getTopRatedCampings() {
        return campingRepository.findTop10ByOrderByRatingDesc();
    }

    public List<Camping> getMostBookedCampings() {
        return campingRepository.findTop10ByOrderByBookingCountDesc();
    }

    public List<Camping> getRecentCampings() {
        return campingRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional
    public void deleteCamping(Long id) {
        Camping camping = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu cắm trại"));

        if (!camping.getBookings().isEmpty()) {
            throw new RuntimeException("Không thể xóa khu cắm trại đã có đặt chỗ");
        }

        campingRepository.delete(camping);
    }

    private void validateCamping(Camping camping) {
        if (camping.getName() == null || camping.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên khu cắm trại không được để trống");
        }
        if (camping.getLocation() == null || camping.getLocation().trim().isEmpty()) {
            throw new RuntimeException("Địa điểm không được để trống");
        }
        if (camping.getPrice() == null || camping.getPrice() <= 0) {
            throw new RuntimeException("Giá phải lớn hơn 0");
        }
        if (camping.getMaxPlaces() == null || camping.getMaxPlaces() <= 0) {
            throw new RuntimeException("Số chỗ phải lớn hơn 0");
        }
        if (camping.getOwner() == null) {
            throw new RuntimeException("Chủ sở hữu không được để trống");
        }
    }

    public boolean bookCamping(Long id, LocalDate startDate, LocalDate endDate, int numberOfPeople, String customerName) {
        return campingRepository.findById(id).map(camping -> {
            if (camping.isAvailable() && numberOfPeople <= camping.getMaxPlaces() && isAvailableForDates(camping, startDate, endDate)) {
                Booking booking = new Booking();
                booking.setCheckIn(startDate.atStartOfDay());
                booking.setCheckOut(endDate.atStartOfDay());
                booking.setGuests(numberOfPeople);
                booking.setStatus(Booking.BookingStatus.PENDING);
                camping.addBooking(booking);
                campingRepository.save(camping);
                return true;
            }
            return false;
        }).orElse(false);
    }

    public boolean cancelBooking(Long id, int bookingIndex) {
        return campingRepository.findById(id).map(camping -> {
            List<Booking> bookings = camping.getBookings();
            if (bookingIndex >= 0 && bookingIndex < bookings.size()) {
                Booking booking = bookings.get(bookingIndex);
                if (booking.getCheckIn().toLocalDate().isAfter(LocalDate.now().plusDays(1))) {
                    booking.setStatus(Booking.BookingStatus.CANCELLED);
                    campingRepository.save(camping);
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    public boolean addReview(Long id, int rating, String comment, String reviewerName) {
        return campingRepository.findById(id).map(camping -> {
            // Update the camping's rating directly
            Double currentRating = camping.getRating();
            Integer currentCount = camping.getBookingCount();
            Double newRating = ((currentRating * currentCount) + rating) / (currentCount + 1);
            camping.setRating(newRating);
            campingRepository.save(camping);
            return true;
        }).orElse(false);
    }

    public Page<Camping> searchCampingsByName(String name, int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort);
        return campingRepository.findByNameContainingIgnoreCase(name, pageRequest);
    }

    public Page<Camping> getCampingsByMinPlaces(int minPlaces, int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort);
        return campingRepository.findByMaxPlacesGreaterThanEqual(minPlaces, pageRequest);
    }

    public Page<Camping> getCampingsByAvailability(boolean isAvailable, int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort);
        return campingRepository.findByIsAvailable(isAvailable, pageRequest);
    }

    public Page<Camping> getCampingsPaginated(int page, int size, String sort, LocalDate startDate, LocalDate endDate) {
        PageRequest pageRequest = buildPageRequest(page, size, sort);
        Page<Camping> campingPage = campingRepository.findAll(pageRequest);
        if (startDate != null && endDate != null) {
            List<Camping> filteredList = campingPage.getContent().stream()
                    .filter(c -> isAvailableForDates(c, startDate, endDate))
                    .collect(Collectors.toList());
            return new PageImpl<>(filteredList, pageRequest, campingPage.getTotalElements());
        }
        return campingPage;
    }

    private boolean isAvailableForDates(Camping camping, LocalDate start, LocalDate end) {
        for (Booking booking : camping.getBookings()) {
            if (booking.getStatus() != Booking.BookingStatus.CANCELLED &&
                    !(end.isBefore(booking.getCheckIn().toLocalDate()) ||
                            start.isAfter(booking.getCheckOut().toLocalDate()))) {
                return false;
            }
        }
        return true;
    }

    private PageRequest buildPageRequest(int page, int size, String sort) {
        if (sort == null || sort.isEmpty()) {
            return PageRequest.of(page, size);
        }
        switch (sort) {
            case "price_asc":
                return PageRequest.of(page, size, Sort.by("price").ascending());
            case "price_desc":
                return PageRequest.of(page, size, Sort.by("price").descending());
            case "maxPlaces_asc":
                return PageRequest.of(page, size, Sort.by("maxPlaces").ascending());
            case "maxPlaces_desc":
                return PageRequest.of(page, size, Sort.by("maxPlaces").descending());
            default:
                return PageRequest.of(page, size);
        }
    }

    public Camping addCamping(Camping camping) {
        validateCamping(camping);
        return campingRepository.save(camping);
    }
}