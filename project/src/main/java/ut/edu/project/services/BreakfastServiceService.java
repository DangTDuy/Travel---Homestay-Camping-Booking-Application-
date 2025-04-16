package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.project.models.BreakfastService;
import ut.edu.project.models.Homestay;
import ut.edu.project.repositories.BreakfastServiceRepository;
import ut.edu.project.repositories.HomestayRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BreakfastServiceService {

    @Autowired
    private BreakfastServiceRepository breakfastServiceRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @Transactional
    public BreakfastService createBreakfastService(BreakfastService breakfastService, Long homestayId) {
        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new RuntimeException("Homestay không tồn tại"));
        
        breakfastService.setHomestay(homestay);
        return breakfastServiceRepository.save(breakfastService);
    }

    @Transactional
    public BreakfastService updateBreakfastService(Long id, BreakfastService breakfastService) {
        BreakfastService existingService = breakfastServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ bữa sáng không tồn tại"));
        
        existingService.setName(breakfastService.getName());
        existingService.setDescription(breakfastService.getDescription());
        existingService.setPrice(breakfastService.getPrice());
        existingService.setIsVegetarian(breakfastService.getIsVegetarian());
        existingService.setIsVegan(breakfastService.getIsVegan());
        existingService.setIsGlutenFree(breakfastService.getIsGlutenFree());
        existingService.setIsDairyFree(breakfastService.getIsDairyFree());
        existingService.setIsAvailable(breakfastService.getIsAvailable());
        
        return breakfastServiceRepository.save(existingService);
    }

    public Optional<BreakfastService> getBreakfastServiceById(Long id) {
        return breakfastServiceRepository.findById(id);
    }

    public List<BreakfastService> getAllBreakfastServices() {
        return breakfastServiceRepository.findAll();
    }

    public List<BreakfastService> getBreakfastServicesByHomestay(Long homestayId) {
        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new RuntimeException("Homestay không tồn tại"));
        
        return breakfastServiceRepository.findByHomestay(homestay);
    }

    public List<BreakfastService> getAvailableBreakfastServices() {
        return breakfastServiceRepository.findByIsAvailableTrue();
    }

    public List<BreakfastService> getVegetarianBreakfastServices() {
        return breakfastServiceRepository.findByIsVegetarianTrue();
    }

    public List<BreakfastService> getVeganBreakfastServices() {
        return breakfastServiceRepository.findByIsVeganTrue();
    }

    public List<BreakfastService> getGlutenFreeBreakfastServices() {
        return breakfastServiceRepository.findByIsGlutenFreeTrue();
    }

    public List<BreakfastService> getDairyFreeBreakfastServices() {
        return breakfastServiceRepository.findByIsDairyFreeTrue();
    }

    @Transactional
    public void deleteBreakfastService(Long id) {
        BreakfastService service = breakfastServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ bữa sáng không tồn tại"));
        
        breakfastServiceRepository.delete(service);
    }
}
