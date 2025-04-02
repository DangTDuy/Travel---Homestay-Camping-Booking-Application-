package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Camping;
import ut.edu.project.repositories.CampingRepository;

import java.util.List;
import java.util.Optional;

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
            camping.setPlaces(campingDetails.getPlaces());
            camping.setDescription(campingDetails.getDescription());
            camping.setAvailable(campingDetails.isAvailable());
            camping.setImage(campingDetails.getImage());
            return campingRepository.save(camping);
        }).orElse(null);
    }

    public void deleteCamping(Long id) {
        campingRepository.deleteById(id);
    }

    public List<Camping> getAvailableCampings() {
        return campingRepository.findByIsAvailableTrue();
    }

    public boolean bookCamping(Long id) {
        return campingRepository.findById(id).map(camping -> {
            if (camping.isAvailable() && camping.getPlaces() > 0) {
                camping.setPlaces(camping.getPlaces() - 1);
                if (camping.getPlaces() == 0) {
                    camping.setAvailable(false);
                }
                campingRepository.save(camping);
                return true;
            }
            return false;
        }).orElse(false);
    }

    public Page<Camping> searchCampingsByName(String name, int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort);
        return campingRepository.findByNameContainingIgnoreCase(name, pageRequest);
    }

    public Page<Camping> getCampingsByMinPlaces(int minPlaces, int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort);
        return campingRepository.findByPlacesGreaterThanEqual(minPlaces, pageRequest);
    }

    public Page<Camping> getCampingsByAvailability(boolean isAvailable, int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort);
        return campingRepository.findByIsAvailable(isAvailable, pageRequest);
    }

    public Page<Camping> getCampingsPaginated(int page, int size, String sort) {
        PageRequest pageRequest = buildPageRequest(page, size, sort);
        return campingRepository.findAll(pageRequest);
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
            case "places_asc":
                return PageRequest.of(page, size, Sort.by("places").ascending());
            case "places_desc":
                return PageRequest.of(page, size, Sort.by("places").descending());
            default:
                return PageRequest.of(page, size);
        }
    }
}