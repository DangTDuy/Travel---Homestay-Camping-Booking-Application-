package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl; // Thêm import cho PageImpl
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Camping;
import ut.edu.project.repositories.CampingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Thêm import cho Collectors

@Service
public class CampingService {
    @Autowired
    private CampingRepository campingRepository;

    public List<Camping> getAllCampings() {
        return campingRepository.findAll();
    }

    public Optional<Camping> getCampingById(Long id) {
        return campingRepository.findById(id);
    }

    public Camping addCamping(Camping camping) {
        return campingRepository.save(camping);
    }

    public Camping updateCamping(Long id, Camping campingDetails) {
        return campingRepository.findById(id).map(camping -> {
            camping.setName(campingDetails.getName());
            camping.setLocation(campingDetails.getLocation());
            camping.setPrice(campingDetails.getPrice());
            camping.setMaxPlaces(campingDetails.getMaxPlaces());
            camping.setDescription(campingDetails.getDescription());
            camping.setAvailable(campingDetails.isAvailable());
            camping.setImage(campingDetails.getImage());
            camping.setAdditionalServices(campingDetails.getAdditionalServices());
            return campingRepository.save(camping);
        }).orElse(null);
    }

    public void deleteCamping(Long id) {
        campingRepository.deleteById(id);
    }

    public List<Camping> getAvailableCampings() {
        return campingRepository.findByIsAvailableTrue();
    }

    public boolean bookCamping(Long id, LocalDate startDate, LocalDate endDate, int numberOfPeople, String customerName) {
        return campingRepository.findById(id).map(camping -> {
            if (camping.isAvailable() && numberOfPeople <= camping.getMaxPlaces() && isAvailableForDates(camping, startDate, endDate)) {
                String booking = startDate + "#" + endDate + "#" + numberOfPeople + "#" + customerName + "#false";
                camping.getBookings().add(booking);
                campingRepository.save(camping);
                return true;
            }
            return false;
        }).orElse(false);
    }

    public boolean cancelBooking(Long id, int bookingIndex) {
        return campingRepository.findById(id).map(camping -> {
            List<String> bookings = camping.getBookings();
            if (bookingIndex >= 0 && bookingIndex < bookings.size()) {
                String[] parts = bookings.get(bookingIndex).split("#");
                LocalDate startDate = LocalDate.parse(parts[0]);
                if (LocalDate.now().isBefore(startDate.minusDays(1))) {
                    bookings.set(bookingIndex, parts[0] + "#" + parts[1] + "#" + parts[2] + "#" + parts[3] + "#true");
                    campingRepository.save(camping);
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    public boolean addReview(Long id, int rating, String comment, String reviewerName) {
        return campingRepository.findById(id).map(camping -> {
            String review = rating + "#" + comment + "#" + reviewerName;
            camping.getReviews().add(review);
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
        for (String booking : camping.getBookings()) {
            String[] parts = booking.split("#");
            LocalDate bookingStart = LocalDate.parse(parts[0]);
            LocalDate bookingEnd = LocalDate.parse(parts[1]);
            boolean isCancelled = Boolean.parseBoolean(parts[4]);
            if (!isCancelled && !(end.isBefore(bookingStart) || start.isAfter(bookingEnd))) {
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
}