package ut.edu.project.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import ut.edu.project.models.Additional;
import ut.edu.project.repositories.AdditionalRepository;
import ut.edu.project.dtos.AdditionalDTO;
import java.util.List;
import java.util.stream.Collectors;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.Category;
import ut.edu.project.models.TimeSlot;
import ut.edu.project.repositories.CategoryRepository;
import ut.edu.project.repositories.TimeSlotRepository;

@Service
public class AdditionalService {
    @Autowired
    private AdditionalRepository additionalRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TimeSlotRepository timeSlotRepository;

    public List<AdditionalDTO> getAllAdditionals() {
        return additionalRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AdditionalDTO getAdditionalById(Long id) {
        return additionalRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Additional not found"));
    }

    public AdditionalDTO createAdditional(AdditionalDTO dto) {
        Additional additional = convertToEntity(dto);
        return convertToDTO(additionalRepository.save(additional));
    }

    public AdditionalDTO updateAdditional(Long id, AdditionalDTO dto) {
        Additional existing = additionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Additional not found"));
        updateEntityFromDTO(existing, dto);
        return convertToDTO(additionalRepository.save(existing));
    }

    public void deleteAdditional(Long id) {
        additionalRepository.deleteById(id);
    }

    public List<Additional> getByHomestay(Homestay homestay) {
        return additionalRepository.findByHomestay(homestay);
    }
    
    /**
     * Lấy danh sách dịch vụ bổ sung theo homestay ID
     * @param homestayId ID của homestay
     * @return Danh sách dịch vụ bổ sung
     */
    public List<Additional> getAdditionalsByHomestayId(Long homestayId) {
        return additionalRepository.findByHomestayId(homestayId);
    }

    public List<Additional> getByIds(List<Long> ids) {
        return additionalRepository.findAllById(ids);
    }

    public AdditionalDTO convertToDTO(Additional entity) {
        AdditionalDTO dto = new AdditionalDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setTimeSlot(entity.getTimeSlot());
        dto.setCategory(entity.getCategory());
        dto.setHomestayId(entity.getHomestay() != null ? entity.getHomestay().getId() : null);
        dto.setCampingId(entity.getCamping() != null ? entity.getCamping().getId() : null);
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setActive(entity.isActive());
        dto.setImageUrl(entity.getImageUrl());
        return dto;
    }

    private Additional convertToEntity(AdditionalDTO dto) {
        Additional entity = new Additional();
        updateEntityFromDTO(entity, dto);
        return entity;
    }

    private void updateEntityFromDTO(Additional entity, AdditionalDTO dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setTimeSlot(dto.getTimeSlot());
        entity.setCategory(dto.getCategory());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setActive(dto.isActive());
        entity.setImageUrl(dto.getImageUrl());
    }
    
    /**
     * Lấy category mặc định cho dịch vụ bổ sung
     * @return Category mặc định hoặc category đầu tiên trong database
     */
    public Category getDefaultCategory() {
        // Tìm category có tên "default" hoặc "mặc định"
        Category defaultCategory = categoryRepository.findByName("default");
        if (defaultCategory == null) {
            defaultCategory = categoryRepository.findByName("mặc định");
        }
        
        // Nếu không tìm thấy, lấy category đầu tiên
        if (defaultCategory == null) {
            List<Category> categories = categoryRepository.findAll();
            if (!categories.isEmpty()) {
                defaultCategory = categories.get(0);
            } else {
                // Nếu không có category nào, tạo một category mới
                defaultCategory = new Category();
                defaultCategory.setName("default");
                defaultCategory.setDisplayName("Mặc định");
                defaultCategory.setActive(true);
                defaultCategory = categoryRepository.save(defaultCategory);
            }
        }
        
        return defaultCategory;
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseGet(this::getDefaultCategory);
    }
    
    /**
     * Lấy time slot mặc định cho dịch vụ bổ sung
     * @return TimeSlot mặc định hoặc time slot đầu tiên trong database
     */
    public TimeSlot getDefaultTimeSlot() {
        // Tìm time slot có tên "default" hoặc "mặc định"
        TimeSlot defaultTimeSlot = timeSlotRepository.findByName("default");
        if (defaultTimeSlot == null) {
            defaultTimeSlot = timeSlotRepository.findByName("mặc định");
        }
        
        // Nếu không tìm thấy, lấy time slot đầu tiên
        if (defaultTimeSlot == null) {
            List<TimeSlot> timeSlots = timeSlotRepository.findAll();
            if (!timeSlots.isEmpty()) {
                defaultTimeSlot = timeSlots.get(0);
            } else {
                // Nếu không có time slot nào, tạo một time slot mới
                defaultTimeSlot = new TimeSlot();
                defaultTimeSlot.setName("default");
                defaultTimeSlot.setDisplayName("Mặc định");
                defaultTimeSlot.setActive(true);
                defaultTimeSlot = timeSlotRepository.save(defaultTimeSlot);
            }
        }
        
        return defaultTimeSlot;
    }

    public TimeSlot getTimeSlotById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseGet(this::getDefaultTimeSlot);
    }
} 