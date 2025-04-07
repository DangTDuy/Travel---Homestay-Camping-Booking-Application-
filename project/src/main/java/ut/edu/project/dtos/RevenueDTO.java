package ut.edu.project.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RevenueDTO {
    private Long id;
    private Long bookingId;
    private BigDecimal totalAmount;
    private BigDecimal adminFee;
    private BigDecimal ownerAmount;
    private String serviceType;
    private LocalDateTime createdAt;
    private String status;
} 