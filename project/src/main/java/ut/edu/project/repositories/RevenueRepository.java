package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Revenue;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Long> {
    List<Revenue> findByBookingId(Long bookingId);
    List<Revenue> findByServiceType(String serviceType);
    List<Revenue> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT SUM(r.totalAmount) FROM Revenue r WHERE r.createdAt BETWEEN ?1 AND ?2")
    BigDecimal calculateTotalRevenue(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT SUM(r.adminFee) FROM Revenue r WHERE r.createdAt BETWEEN ?1 AND ?2")
    BigDecimal calculateTotalAdminFee(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT SUM(r.ownerAmount) FROM Revenue r WHERE r.createdAt BETWEEN ?1 AND ?2")
    BigDecimal calculateTotalOwnerAmount(LocalDateTime start, LocalDateTime end);
} 