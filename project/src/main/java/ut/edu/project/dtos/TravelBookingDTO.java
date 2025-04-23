package ut.edu.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelBookingDTO {
    private Long travelId;
    private Integer numberOfPeople;
    private LocalDateTime startDate;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String specialRequests;
    private List<AdditionalServiceDTO> additionalServices = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalServiceDTO {
        private Long id;
        private Integer quantity = 1;
    }
}
