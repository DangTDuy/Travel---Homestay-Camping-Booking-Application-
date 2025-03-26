package ut.edu.project.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Camping;
import ut.edu.project.repositories.CampingRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampingService {
    private final CampingRepository campingRepository;

    public List<Camping> getAllCampings() {
        return campingRepository.findAll();
    }

    public Optional<Camping> getCampingById(Long id) {
        return campingRepository.findById(id);
    }

    public Camping saveCamping(Camping camping) {
        return campingRepository.save(camping);
    }

    public void deleteCamping(Long id) {
        campingRepository.deleteById(id);
    }
}