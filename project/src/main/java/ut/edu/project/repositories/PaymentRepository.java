package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ut.edu.project.models.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Các phương thức truy vấn tuỳ chỉnh có thể được thêm ở đây
}