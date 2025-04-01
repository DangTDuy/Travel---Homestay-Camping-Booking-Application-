package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
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

    public void deleteCamping(Long id) {
        campingRepository.deleteById(id);
    }

    public List<Camping> getAvailableCampings() {
        return campingRepository.findByIsAvailable(true);
    }
}
