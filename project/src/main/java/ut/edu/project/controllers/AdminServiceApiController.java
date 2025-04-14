package ut.edu.project.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.dtos.AdditionalDTO;
import ut.edu.project.models.Category;
import ut.edu.project.models.TimeSlot;
import ut.edu.project.repositories.CategoryRepository;
import ut.edu.project.repositories.TimeSlotRepository;
import ut.edu.project.services.AdditionalService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
@Slf4j
public class AdminServiceApiController {

    private final AdditionalService additionalService;
    private final CategoryRepository categoryRepository;
    private final TimeSlotRepository timeSlotRepository;

    @GetMapping("/{id}")
    public ResponseEntity<AdditionalDTO> getService(@PathVariable Long id) {
        try {
            AdditionalDTO service = additionalService.getAdditionalById(id);
            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createService(@RequestBody Map<String, Object> requestData) {
        try {
            log.info("Creating service with data: {}", requestData);

            AdditionalDTO dto = new AdditionalDTO();
            dto.setName((String) requestData.get("name"));
            dto.setDescription((String) requestData.get("description"));

            // Parse numeric values
            if (requestData.get("price") != null) {
                if (requestData.get("price") instanceof Number) {
                    dto.setPrice(java.math.BigDecimal.valueOf(((Number) requestData.get("price")).doubleValue()));
                } else {
                    dto.setPrice(new java.math.BigDecimal(requestData.get("price").toString()));
                }
            }

            // Handle category
            if (requestData.get("categoryId") != null) {
                Long categoryId;
                if (requestData.get("categoryId") instanceof Number) {
                    categoryId = ((Number) requestData.get("categoryId")).longValue();
                } else {
                    categoryId = Long.parseLong(requestData.get("categoryId").toString());
                }
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                dto.setCategory(category);
            }

            // Handle timeSlot
            if (requestData.get("timeSlotId") != null) {
                Long timeSlotId;
                if (requestData.get("timeSlotId") instanceof Number) {
                    timeSlotId = ((Number) requestData.get("timeSlotId")).longValue();
                } else {
                    timeSlotId = Long.parseLong(requestData.get("timeSlotId").toString());
                }
                TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                        .orElseThrow(() -> new RuntimeException("TimeSlot not found"));
                dto.setTimeSlot(timeSlot);
            }

            // Handle homestay
            if (requestData.get("homestayId") != null) {
                Long homestayId;
                if (requestData.get("homestayId") instanceof Number) {
                    homestayId = ((Number) requestData.get("homestayId")).longValue();
                } else {
                    homestayId = Long.parseLong(requestData.get("homestayId").toString());
                }

                // Nếu homestayId = -1, đặt là null để áp dụng cho tất cả homestay
                if (homestayId == -1) {
                    dto.setHomestayId(null);
                } else {
                    dto.setHomestayId(homestayId);
                }
            }

            // Handle camping
            if (requestData.get("campingId") != null) {
                Long campingId;
                if (requestData.get("campingId") instanceof Number) {
                    campingId = ((Number) requestData.get("campingId")).longValue();
                } else {
                    campingId = Long.parseLong(requestData.get("campingId").toString());
                }

                // Nếu campingId = -1, đặt là null để áp dụng cho tất cả camping
                if (campingId == -1) {
                    dto.setCampingId(null);
                } else {
                    dto.setCampingId(campingId);
                }
            }

            // Handle travel
            if (requestData.get("travelId") != null) {
                Long travelId;
                if (requestData.get("travelId") instanceof Number) {
                    travelId = ((Number) requestData.get("travelId")).longValue();
                } else {
                    travelId = Long.parseLong(requestData.get("travelId").toString());
                }

                // Nếu travelId = -1, đặt là null để áp dụng cho tất cả travel
                if (travelId == -1) {
                    dto.setTravelId(null);
                } else {
                    dto.setTravelId(travelId);
                }
            }

            // Handle serviceType
            if (requestData.get("serviceType") != null) {
                String serviceTypeStr = (String) requestData.get("serviceType");
                try {
                    ut.edu.project.models.Additional.ServiceType serviceType =
                        ut.edu.project.models.Additional.ServiceType.valueOf(serviceTypeStr);
                    dto.setServiceType(serviceType);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid serviceType: {}", serviceTypeStr);
                }
            }

            // Handle time fields
            if (requestData.get("startTime") != null) {
                dto.setStartTime(java.time.LocalTime.parse((String) requestData.get("startTime")));
            }

            if (requestData.get("endTime") != null) {
                dto.setEndTime(java.time.LocalTime.parse((String) requestData.get("endTime")));
            }

            // Handle active status
            log.info("isActive value from request: {}, type: {}", requestData.get("isActive"),
                    requestData.get("isActive") != null ? requestData.get("isActive").getClass().getName() : "null");

            if (requestData.get("isActive") != null) {
                boolean isActive;
                if (requestData.get("isActive") instanceof Boolean) {
                    isActive = (Boolean) requestData.get("isActive");
                } else {
                    // Convert from string or other types
                    isActive = Boolean.parseBoolean(requestData.get("isActive").toString());
                }
                dto.setActive(isActive);
                log.info("Setting isActive to: {}", isActive);
            } else {
                // Mặc định là hoạt động nếu không có giá trị
                dto.setActive(true);
                log.info("No isActive value provided, defaulting to true");
            }

            AdditionalDTO createdService = additionalService.createAdditional(dto);
            return ResponseEntity.ok(createdService);
        } catch (Exception e) {
            log.error("Error creating service", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(
            @PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        try {
            log.info("Updating service {} with data: {}", id, requestData);

            AdditionalDTO dto = additionalService.getAdditionalById(id);

            // Update fields from request
            if (requestData.get("name") != null) {
                dto.setName((String) requestData.get("name"));
            }

            if (requestData.get("description") != null) {
                dto.setDescription((String) requestData.get("description"));
            }

            // Parse numeric values
            if (requestData.get("price") != null) {
                if (requestData.get("price") instanceof Number) {
                    dto.setPrice(java.math.BigDecimal.valueOf(((Number) requestData.get("price")).doubleValue()));
                } else {
                    dto.setPrice(new java.math.BigDecimal(requestData.get("price").toString()));
                }
            }

            // Handle category
            if (requestData.get("categoryId") != null) {
                Long categoryId;
                if (requestData.get("categoryId") instanceof Number) {
                    categoryId = ((Number) requestData.get("categoryId")).longValue();
                } else {
                    categoryId = Long.parseLong(requestData.get("categoryId").toString());
                }
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                dto.setCategory(category);
            }

            // Handle timeSlot
            if (requestData.get("timeSlotId") != null) {
                Long timeSlotId;
                if (requestData.get("timeSlotId") instanceof Number) {
                    timeSlotId = ((Number) requestData.get("timeSlotId")).longValue();
                } else {
                    timeSlotId = Long.parseLong(requestData.get("timeSlotId").toString());
                }
                TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                        .orElseThrow(() -> new RuntimeException("TimeSlot not found"));
                dto.setTimeSlot(timeSlot);
            }

            // Handle homestay
            if (requestData.get("homestayId") != null) {
                Long homestayId;
                if (requestData.get("homestayId") instanceof Number) {
                    homestayId = ((Number) requestData.get("homestayId")).longValue();
                } else {
                    homestayId = Long.parseLong(requestData.get("homestayId").toString());
                }

                // Nếu homestayId = -1, đặt là null để áp dụng cho tất cả homestay
                if (homestayId == -1) {
                    dto.setHomestayId(null);
                } else {
                    dto.setHomestayId(homestayId);
                }
            }

            // Handle camping
            if (requestData.get("campingId") != null) {
                Long campingId;
                if (requestData.get("campingId") instanceof Number) {
                    campingId = ((Number) requestData.get("campingId")).longValue();
                } else {
                    campingId = Long.parseLong(requestData.get("campingId").toString());
                }

                // Nếu campingId = -1, đặt là null để áp dụng cho tất cả camping
                if (campingId == -1) {
                    dto.setCampingId(null);
                } else {
                    dto.setCampingId(campingId);
                }
            }

            // Handle travel
            if (requestData.get("travelId") != null) {
                Long travelId;
                if (requestData.get("travelId") instanceof Number) {
                    travelId = ((Number) requestData.get("travelId")).longValue();
                } else {
                    travelId = Long.parseLong(requestData.get("travelId").toString());
                }

                // Nếu travelId = -1, đặt là null để áp dụng cho tất cả travel
                if (travelId == -1) {
                    dto.setTravelId(null);
                } else {
                    dto.setTravelId(travelId);
                }
            }

            // Handle serviceType
            if (requestData.get("serviceType") != null) {
                String serviceTypeStr = (String) requestData.get("serviceType");
                try {
                    ut.edu.project.models.Additional.ServiceType serviceType =
                        ut.edu.project.models.Additional.ServiceType.valueOf(serviceTypeStr);
                    dto.setServiceType(serviceType);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid serviceType: {}", serviceTypeStr);
                }
            }

            // Handle time fields
            if (requestData.get("startTime") != null) {
                dto.setStartTime(java.time.LocalTime.parse((String) requestData.get("startTime")));
            }

            if (requestData.get("endTime") != null) {
                dto.setEndTime(java.time.LocalTime.parse((String) requestData.get("endTime")));
            }

            // Handle active status
            log.info("isActive value from request: {}, type: {}", requestData.get("isActive"),
                    requestData.get("isActive") != null ? requestData.get("isActive").getClass().getName() : "null");

            if (requestData.get("isActive") != null) {
                boolean isActive;
                if (requestData.get("isActive") instanceof Boolean) {
                    isActive = (Boolean) requestData.get("isActive");
                } else {
                    // Convert from string or other types
                    isActive = Boolean.parseBoolean(requestData.get("isActive").toString());
                }
                dto.setActive(isActive);
                log.info("Setting isActive to: {}", isActive);
            } else {
                // Giữ nguyên trạng thái hoạt động hiện tại nếu không có giá trị mới
                log.info("No isActive value provided, keeping current value: {}", dto.isActive());
            }

            AdditionalDTO updatedService = additionalService.updateAdditional(id, dto);
            return ResponseEntity.ok(updatedService);
        } catch (Exception e) {
            log.error("Error updating service", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        additionalService.deleteAdditional(id);
        return ResponseEntity.ok().build();
    }
}
