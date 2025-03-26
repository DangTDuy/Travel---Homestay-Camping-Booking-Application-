package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Travel;
import ut.edu.project.repositories.TravelRepository;

import java.util.List;

@Service
public class TravelService {
    @Autowired
    private TravelRepository travelRepository;

    public List<Travel> getAllTravels() {
        return travelRepository.findAll();
    }

    public Travel getTravelById(Long id) {
        return travelRepository.findById(id).orElse(null);
    }

    public Travel createTravel(Travel travel) {
        return travelRepository.save(travel);
    }

    public void deleteTravel(Long id) {
        travelRepository.deleteById(id);
    }
}
