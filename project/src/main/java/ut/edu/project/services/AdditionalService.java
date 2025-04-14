package ut.edu.project.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import ut.edu.project.models.Additional;
import ut.edu.project.repositories.AdditionalRepository;
import ut.edu.project.dtos.AdditionalDTO;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.Camping;
import ut.edu.project.models.Travel;
import ut.edu.project.models.Category;
import ut.edu.project.models.TimeSlot;
import ut.edu.project.repositories.CategoryRepository;
import ut.edu.project.repositories.TimeSlotRepository;
import ut.edu.project.repositories.HomestayRepository;
import ut.edu.project.repositories.CampingRepository;
import ut.edu.project.repositories.TravelRepository;

@Service
public class AdditionalService {
    @Autowired
    private AdditionalRepository additionalRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private CampingRepository campingRepository;

    @Autowired
    private TravelRepository travelRepository;

    /**
     * Lấy tất cả dịch vụ bổ sung
     * @return Danh sách tất cả dịch vụ bổ sung
     */
    public List<AdditionalDTO> getAllAdditionals() {
        return additionalRepository.findAll().stream()
                .filter(additional -> !additional.getName().startsWith("[DELETED]")) // Lọc bỏ các dịch vụ đã xóa
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả dịch vụ bổ sung đang hoạt động
     * @return Danh sách tất cả dịch vụ bổ sung đang hoạt động
     */
    public List<AdditionalDTO> getAllActiveAdditionals() {
        return additionalRepository.findByIsActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy các dịch vụ bổ sung chung (không gắn với homestay cụ thể)
     * @return Danh sách dịch vụ bổ sung chung
     */
    public List<AdditionalDTO> getCommonAdditionals() {
        return additionalRepository.findByHomestayIsNull().stream()
                .filter(Additional::isActive) // Chỉ lấy dịch vụ đang hoạt động
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

    /**
     * Xóa dịch vụ bổ sung (soft delete)
     * Thay vì xóa hoàn toàn, chỉ đánh dấu là không hoạt động
     * @param id ID của dịch vụ cần xóa
     */
    public void deleteAdditional(Long id) {
        try {
            Additional additional = additionalRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Additional not found"));

            // Soft delete: đánh dấu là không hoạt động thay vì xóa
            additional.setActive(false);

            // Đổi tên để biết là đã bị xóa
            additional.setName("[DELETED] " + additional.getName());

            // Lưu lại vào database
            additionalRepository.save(additional);
        } catch (Exception e) {
            throw new RuntimeException("Không thể xóa dịch vụ: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách dịch vụ bổ sung cho homestay, bao gồm cả dịch vụ chung
     * @param homestay Homestay cần lấy dịch vụ
     * @return Danh sách dịch vụ bổ sung
     */
    public List<Additional> getByHomestay(Homestay homestay) {
        // Lấy dịch vụ riêng của homestay
        List<Additional> homestayServices = additionalRepository.findByHomestay(homestay);

        // Lấy dịch vụ chung (không gắn với homestay cụ thể)
        List<Additional> commonServices = additionalRepository.findByHomestayIsNull();

        // Kết hợp cả hai danh sách
        List<Additional> allServices = new ArrayList<>();
        allServices.addAll(homestayServices);
        allServices.addAll(commonServices);

        return allServices;
    }

    /**
     * Lấy danh sách dịch vụ bổ sung theo homestay ID, bao gồm cả dịch vụ chung
     * @param homestayId ID của homestay
     * @return Danh sách dịch vụ bổ sung dưới dạng DTO
     */
    public List<AdditionalDTO> getAdditionalsByHomestayId(Long homestayId) {
        // Lấy dịch vụ riêng của homestay (chỉ lấy dịch vụ đang hoạt động)
        List<AdditionalDTO> homestayServices = additionalRepository.findByHomestayId(homestayId).stream()
                .filter(Additional::isActive) // Chỉ lấy dịch vụ đang hoạt động
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Lấy dịch vụ chung (không gắn với homestay cụ thể)
        List<AdditionalDTO> commonServices = getCommonAdditionals();

        // Tạo một danh sách mới để lưu tất cả dịch vụ
        List<AdditionalDTO> allServices = new ArrayList<>();

        // Thêm dịch vụ riêng của homestay
        allServices.addAll(homestayServices);

        // Thêm dịch vụ chung, loại bỏ các dịch vụ trùng lặp và chỉ lấy dịch vụ cho HOMESTAY hoặc ALL
        for (AdditionalDTO commonService : commonServices) {
            // Kiểm tra xem dịch vụ chung đã có trong danh sách dịch vụ riêng của homestay chưa
            boolean isDuplicate = false;
            for (AdditionalDTO homestayService : homestayServices) {
                if (commonService.getName().equals(homestayService.getName()) &&
                    (commonService.getCategory() == null || homestayService.getCategory() == null ||
                     commonService.getCategory().getId().equals(homestayService.getCategory().getId()))) {
                    isDuplicate = true;
                    break;
                }
            }

            // Nếu không trùng lặp và là dịch vụ cho HOMESTAY hoặc ALL, thêm vào danh sách
            if (!isDuplicate && (commonService.getServiceType() == Additional.ServiceType.HOMESTAY ||
                                 commonService.getServiceType() == Additional.ServiceType.ALL)) {
                allServices.add(commonService);
            }
        }

        return allServices;
    }

    public List<Additional> getByIds(List<Long> ids) {
        return additionalRepository.findAllById(ids);
    }

    /**
     * Lấy danh sách dịch vụ bổ sung theo travel ID, bao gồm cả dịch vụ chung
     * @param travelId ID của travel
     * @return Danh sách dịch vụ bổ sung dưới dạng DTO
     */
    public List<AdditionalDTO> getAdditionalsByTravelId(Long travelId) {
        // Lấy dịch vụ riêng của travel (chỉ lấy dịch vụ đang hoạt động)
        List<AdditionalDTO> travelServices = additionalRepository.findByTravelId(travelId).stream()
                .filter(Additional::isActive) // Chỉ lấy dịch vụ đang hoạt động
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Lấy dịch vụ chung (không gắn với homestay cụ thể)
        List<AdditionalDTO> commonServices = getCommonAdditionals();

        // Tạo một danh sách mới để lưu tất cả dịch vụ
        List<AdditionalDTO> allServices = new ArrayList<>();

        // Thêm dịch vụ riêng của travel
        allServices.addAll(travelServices);

        // Thêm dịch vụ chung, loại bỏ các dịch vụ trùng lặp và chỉ lấy dịch vụ cho TRAVEL hoặc ALL
        for (AdditionalDTO commonService : commonServices) {
            // Kiểm tra xem dịch vụ chung đã có trong danh sách dịch vụ riêng của travel chưa
            boolean isDuplicate = false;
            for (AdditionalDTO travelService : travelServices) {
                if (commonService.getName().equals(travelService.getName()) &&
                    (commonService.getCategory() == null || travelService.getCategory() == null ||
                     commonService.getCategory().getId().equals(travelService.getCategory().getId()))) {
                    isDuplicate = true;
                    break;
                }
            }

            // Nếu không trùng lặp và là dịch vụ cho TRAVEL hoặc ALL, thêm vào danh sách
            if (!isDuplicate && (commonService.getServiceType() == Additional.ServiceType.TRAVEL ||
                                 commonService.getServiceType() == Additional.ServiceType.ALL)) {
                allServices.add(commonService);
            }
        }

        return allServices;
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
        dto.setTravelId(entity.getTravel() != null ? entity.getTravel().getId() : null);
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setActive(entity.isActive());
        dto.setImageUrl(entity.getImageUrl());
        dto.setQuantity(entity.getQuantity());
        dto.setServiceType(entity.getServiceType());
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
        entity.setQuantity(dto.getQuantity());
        entity.setServiceType(dto.getServiceType());

        // Set homestay if provided
        if (dto.getHomestayId() != null && dto.getHomestayId() > 0) {
            Homestay homestay = homestayRepository.findById(dto.getHomestayId())
                    .orElseThrow(() -> new RuntimeException("Homestay not found"));
            entity.setHomestay(homestay);

            // Nếu gắn với homestay, đặt loại dịch vụ là HOMESTAY
            if (dto.getServiceType() == null || dto.getServiceType() == Additional.ServiceType.ALL) {
                entity.setServiceType(Additional.ServiceType.HOMESTAY);
            }
        } else {
            entity.setHomestay(null);
        }

        // Set camping if provided
        if (dto.getCampingId() != null && dto.getCampingId() > 0) {
            Camping camping = campingRepository.findById(dto.getCampingId())
                    .orElseThrow(() -> new RuntimeException("Camping not found"));
            entity.setCamping(camping);

            // Nếu gắn với camping, đặt loại dịch vụ là CAMPING
            if (dto.getServiceType() == null || dto.getServiceType() == Additional.ServiceType.ALL) {
                entity.setServiceType(Additional.ServiceType.CAMPING);
            }
        } else {
            entity.setCamping(null);
        }

        // Set travel if provided
        if (dto.getTravelId() != null && dto.getTravelId() > 0) {
            Travel travel = travelRepository.findById(dto.getTravelId())
                    .orElseThrow(() -> new RuntimeException("Travel not found"));
            entity.setTravel(travel);

            // Nếu gắn với travel, đặt loại dịch vụ là TRAVEL
            if (dto.getServiceType() == null || dto.getServiceType() == Additional.ServiceType.ALL) {
                entity.setServiceType(Additional.ServiceType.TRAVEL);
            }
        } else {
            entity.setTravel(null);
        }
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