package ut.edu.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
