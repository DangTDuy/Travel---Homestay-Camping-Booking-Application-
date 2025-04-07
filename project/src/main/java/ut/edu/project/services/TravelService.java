package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.project.models.Travel;
import ut.edu.project.models.User;
import ut.edu.project.repositories.TravelRepository;
import ut.edu.project.models.Travel.DifficultyLevel;

import java.util.List;
import java.util.Optional;

@Service
public class TravelService {

    @Autowired
    private TravelRepository travelRepository;

    @Transactional
    public Travel createTravel(Travel travel) {
        validateTravel(travel);
        return travelRepository.save(travel);
    }

    @Transactional
    public Travel updateTravel(Long id, Travel travel) {
        Travel existingTravel = travelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));

        // Cập nhật thông tin
        existingTravel.setTourName(travel.getTourName());
        existingTravel.setLocation(travel.getLocation());
        existingTravel.setDescription(travel.getDescription());
        existingTravel.setPrice(travel.getPrice());
        existingTravel.setItinerary(travel.getItinerary());
        existingTravel.setGuide(travel.getGuide());
        existingTravel.setMaxParticipants(travel.getMaxParticipants());
        existingTravel.setMinParticipants(travel.getMinParticipants());
        existingTravel.setDurationDays(travel.getDurationDays());
        existingTravel.setImageUrls(travel.getImageUrls());
        existingTravel.setIncludedServices(travel.getIncludedServices());
        existingTravel.setRequirements(travel.getRequirements());
        existingTravel.setHighlights(travel.getHighlights());
        existingTravel.setDifficultyLevel(travel.getDifficultyLevel());
        existingTravel.setAvailable(travel.isAvailable());

        validateTravel(existingTravel);
        return travelRepository.save(existingTravel);
    }

    public Optional<Travel> getTravelById(Long id) {
        return travelRepository.findById(id);
    }

    public List<Travel> getAllTravels() {
        return travelRepository.findAll();
    }

    public List<Travel> searchTravels(
            String location,
            Double minPrice,
            Double maxPrice,
            Integer minDays,
            Integer maxDays,
            DifficultyLevel difficultyLevel,
            Boolean isAvailable) {
        return travelRepository.searchTravels(
                location, minPrice, maxPrice, minDays, maxDays, difficultyLevel, isAvailable);
    }

    public List<Travel> getTravelsByGuide(User guide) {
        return travelRepository.findByGuide(guide);
    }

    public List<Travel> getAvailableTravels() {
        return travelRepository.findByIsAvailableTrue();
    }

    public List<Travel> getTravelsByDifficultyLevel(DifficultyLevel level) {
        return travelRepository.findByDifficultyLevel(level);
    }

    public List<Travel> getTravelsByDuration(Integer minDays, Integer maxDays) {
        return travelRepository.findByDurationDaysBetween(minDays, maxDays);
    }

    public List<Travel> getTravelsByIncludedServices(List<String> services) {
        return travelRepository.findByIncludedServicesIn(services);
    }

    public List<Travel> getConfirmedTours() {
        return travelRepository.findConfirmedTours();
    }

    public List<Travel> getAvailableTours() {
        return travelRepository.findAvailableTours();
    }

    public List<Travel> getTopRatedTravels() {
        return travelRepository.findTop10ByOrderByRatingDesc();
    }

    public List<Travel> getMostBookedTravels() {
        return travelRepository.findTop10ByOrderByBookingCountDesc();
    }

    public List<Travel> getRecentTravels() {
        return travelRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional
    public void deleteTravel(Long id) {
        Travel travel = travelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));

        if (!travel.getBookings().isEmpty()) {
            throw new RuntimeException("Không thể xóa tour đã có đặt chỗ");
        }

        travelRepository.delete(travel);
    }

    private void validateTravel(Travel travel) {
        if (travel.getTourName() == null || travel.getTourName().trim().isEmpty()) {
            throw new RuntimeException("Tên tour không được để trống");
        }
        if (travel.getLocation() == null || travel.getLocation().trim().isEmpty()) {
            throw new RuntimeException("Địa điểm không được để trống");
        }
        if (travel.getPrice() == null || travel.getPrice() <= 0) {
            throw new RuntimeException("Giá phải lớn hơn 0");
        }
        if (travel.getItinerary() == null || travel.getItinerary().trim().isEmpty()) {
            throw new RuntimeException("Lịch trình không được để trống");
        }
        if (travel.getGuide() == null) {
            throw new RuntimeException("Hướng dẫn viên không được để trống");
        }
        if (travel.getMaxParticipants() == null || travel.getMaxParticipants() <= 0) {
            throw new RuntimeException("Số người tham gia tối đa phải lớn hơn 0");
        }
        if (travel.getMinParticipants() == null || travel.getMinParticipants() <= 0) {
            throw new RuntimeException("Số người tham gia tối thiểu phải lớn hơn 0");
        }
        if (travel.getMinParticipants() > travel.getMaxParticipants()) {
            throw new RuntimeException("Số người tham gia tối thiểu phải nhỏ hơn hoặc bằng số người tối đa");
        }
        if (travel.getDurationDays() == null || travel.getDurationDays() <= 0) {
            throw new RuntimeException("Số ngày tour phải lớn hơn 0");
        }
    }

    public long getTotalTravelCount() {
        return travelRepository.count();
    }

    public long getAvailableTravelCount() {
        return travelRepository.countByIsAvailableTrue();
    }

    public Travel saveTravel(Travel travel) {
        return travelRepository.save(travel);
    }
}
