package ut.edu.project.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.dtos.AdditionalDTO;
import ut.edu.project.models.Travel;
import ut.edu.project.services.AdditionalService;
import ut.edu.project.services.TravelService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travels")
public class TravelApiController {

    @Autowired
    private TravelService travelService;

    @Autowired
    private AdditionalService additionalService;

    /**
     * Lấy danh sách dịch vụ bổ sung cho tour
     * @param id ID của tour
     * @return Danh sách dịch vụ bổ sung
     */
    @GetMapping("/{id}/additionals")
    public ResponseEntity<?> getTravelAdditionals(@PathVariable Long id) {
        try {
            // Kiểm tra tour tồn tại
            Travel travel = travelService.getTravelById(id)
                    .orElseThrow(() -> new RuntimeException("Tour du lịch không tồn tại"));

            // Lấy danh sách dịch vụ bổ sung
            List<AdditionalDTO> additionals = additionalService.getAdditionalsByTravelId(id);
            return ResponseEntity.ok(additionals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
