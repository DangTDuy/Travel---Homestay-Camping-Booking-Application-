package ut.edu.project.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class BookingDTO {
    private Long id;
    private Long userId;
    private Long homestayId;
    private String serviceType;
    private Long serviceId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private BigDecimal adminFee;
    private BigDecimal ownerAmount;
    private String status;
    private String paymentStatus;
    private String qrCode;
    private List<AdditionalDTO> additionalServices;
} 