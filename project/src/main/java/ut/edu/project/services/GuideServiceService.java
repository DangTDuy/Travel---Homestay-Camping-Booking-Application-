package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.project.models.GuideService;
import ut.edu.project.models.Travel;
import ut.edu.project.repositories.GuideServiceRepository;
import ut.edu.project.repositories.TravelRepository;

import java.util.List;
import java.util.Optional;

@Service
public class GuideServiceService {

    @Autowired
    private GuideServiceRepository guideServiceRepository;

    @Autowired
    private TravelRepository travelRepository;

    @Transactional
    public GuideService createGuideService(GuideService guideService, Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new RuntimeException("Tour du lịch không tồn tại"));
        
        guideService.setTravel(travel);
        return guideServiceRepository.save(guideService);
    }

    @Transactional
    public GuideService updateGuideService(Long id, GuideService guideService) {
        GuideService existingService = guideServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ hướng dẫn viên không tồn tại"));
        
        existingService.setName(guideService.getName());
        existingService.setDescription(guideService.getDescription());
        existingService.setPrice(guideService.getPrice());
        existingService.setLanguage(guideService.getLanguage());
        existingService.setExperienceYears(guideService.getExperienceYears());
        existingService.setIsAvailable(guideService.getIsAvailable());
        existingService.setSpecialties(guideService.getSpecialties());
        
        return guideServiceRepository.save(existingService);
    }

    public Optional<GuideService> getGuideServiceById(Long id) {
        return guideServiceRepository.findById(id);
    }

    public List<GuideService> getAllGuideServices() {
        return guideServiceRepository.findAll();
    }

    public List<GuideService> getGuideServicesByTravel(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new RuntimeException("Tour du lịch không tồn tại"));
        
        return guideServiceRepository.findByTravel(travel);
    }

    public List<GuideService> getAvailableGuideServices() {
        return guideServiceRepository.findByIsAvailableTrue();
    }

    public List<GuideService> getGuideServicesByLanguage(String language) {
        return guideServiceRepository.findByLanguage(language);
    }

    public List<GuideService> getGuideServicesByExperienceYears(Integer years) {
        return guideServiceRepository.findByExperienceYearsGreaterThanEqual(years);
    }

    public List<GuideService> getGuideServicesBySpecialty(String specialty) {
        return guideServiceRepository.findBySpecialty(specialty);
    }

    @Transactional
    public void deleteGuideService(Long id) {
        GuideService service = guideServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ hướng dẫn viên không tồn tại"));
        
        guideServiceRepository.delete(service);
    }
}
