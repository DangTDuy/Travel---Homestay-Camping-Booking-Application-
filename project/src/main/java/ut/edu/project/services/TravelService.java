package ut.edu.project.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Travel;
import ut.edu.project.repositories.TravelRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor  // Tự động tạo constructor cho dependency injection
public class TravelService {

    private final TravelRepository travelRepository;

    public List<Travel> getAllTravels() {
        return travelRepository.findAll();
    }

    public Travel getTravelById(Long id) {
        return travelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Travel with ID " + id + " not found"));
    }

    public Travel createTravel(Travel travel) {
        log.info("Creating new travel: {}", travel);
        return travelRepository.save(travel);
    }

    public Travel updateTravel(Long id, Travel newTravel) {
        return travelRepository.findById(id)
                .map(travel -> {
                    travel.clone();
                    travel.setLocation(newTravel.getLocation());
                    travel.setPrice(newTravel.getPrice());
                    travel.setItinerary(newTravel.getItinerary());
                    travel.setGuide(newTravel.getGuide());
                    return travelRepository.save(travel);
                }).orElseThrow(() -> new RuntimeException("Travel with ID " + id + " not found"));
    }

    public void deleteTravel(Long id) {
        if (!travelRepository.existsById(id)) {
            throw new RuntimeException("Travel with ID " + id + " does not exist");
        }
        travelRepository.deleteById(id);
        log.info("Deleted travel with ID: {}", id);
    }
}
