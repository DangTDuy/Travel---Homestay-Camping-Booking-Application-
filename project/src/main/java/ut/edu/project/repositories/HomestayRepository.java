package ut.edu.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;

import java.util.List;

public interface HomestayRepository extends JpaRepository<Homestay, Long> {
    List<Homestay> findByOwner(User owner);
    List<Homestay> findByLocationContainingIgnoreCase(String location);
    @Query("SELECT h FROM Homestay h WHERE h.tags LIKE %:tag%")
    List<Homestay> findByTag(@Param("tag") String tag);

    @Query("SELECT h FROM Homestay h WHERE " +
           "(:location IS NULL OR h.location LIKE %:location%) AND " +
           "(:minPrice IS NULL OR h.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR h.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR h.rating >= :minRating)")
    List<Homestay> searchHomestays(
            @Param("location") String location,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minRating") Double minRating
    );
}