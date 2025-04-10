package ut.edu.project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ut.edu.project.models.Additional;
import ut.edu.project.models.Category;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.TimeSlot;

import java.util.List;

public interface AdditionalServiceRepository extends AdditionalRepository {

    List<Additional> findByHomestayAndIsActiveTrue(Homestay homestay);

    @Query("SELECT s FROM Additional s WHERE " +
           "(:categoryId IS NULL OR s.category.id = :categoryId) AND " +
           "(:timeSlotId IS NULL OR s.timeSlot.id = :timeSlotId) AND " +
           "(:homestayId IS NULL OR s.homestay.id = :homestayId) AND " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Additional> findByFilters(
            @Param("categoryId") Long categoryId,
            @Param("timeSlotId") Long timeSlotId,
            @Param("homestayId") Long homestayId,
            @Param("search") String search,
            Pageable pageable);

    List<Additional> findByCategoryAndIsActiveTrue(Category category);

    List<Additional> findByTimeSlotAndIsActiveTrue(TimeSlot timeSlot);
}
