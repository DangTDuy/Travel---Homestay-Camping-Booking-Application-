package ut.edu.project.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import ut.edu.project.repositories.RevenueRepository;
import ut.edu.project.dtos.RevenueDTO;
import ut.edu.project.models.Revenue;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RevenueService {
    @Autowired
    private RevenueRepository revenueRepository;

    public List<RevenueDTO> getAllRevenues() {
        return revenueRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RevenueDTO getRevenueById(Long id) {
        return revenueRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Revenue not found"));
    }

    public List<RevenueDTO> getRevenuesByDateRange(LocalDateTime start, LocalDateTime end) {
        return revenueRepository.findByCreatedAtBetween(start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal calculateTotalRevenue(LocalDateTime start, LocalDateTime end) {
        return revenueRepository.calculateTotalRevenue(start, end);
    }

    public BigDecimal calculateTotalAdminFee(LocalDateTime start, LocalDateTime end) {
        return revenueRepository.calculateTotalAdminFee(start, end);
    }

    public BigDecimal calculateTotalOwnerAmount(LocalDateTime start, LocalDateTime end) {
        return revenueRepository.calculateTotalOwnerAmount(start, end);
    }

    private RevenueDTO convertToDTO(Revenue entity) {
        RevenueDTO dto = new RevenueDTO();
        dto.setId(entity.getId());
        dto.setBookingId(entity.getBookingId());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setAdminFee(entity.getAdminFee());
        dto.setOwnerAmount(entity.getOwnerAmount());
        dto.setServiceType(entity.getServiceType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setStatus(entity.getStatus().getName());
        return dto;
    }
} 