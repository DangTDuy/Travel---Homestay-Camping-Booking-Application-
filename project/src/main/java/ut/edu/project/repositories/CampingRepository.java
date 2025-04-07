package ut.edu.project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.project.models.Camping;
import ut.edu.project.models.User;

import java.util.List;

@Repository
public interface CampingRepository extends JpaRepository<Camping, Long> {
    // Tìm theo location
    List<Camping> findByLocationContainingIgnoreCase(String location);

    // Tìm theo khoảng giá
    List<Camping> findByPriceBetween(Double minPrice, Double maxPrice);

    // Tìm theo số chỗ trống
    List<Camping> findByMaxPlacesGreaterThanEqual(Integer places);

    // Tìm theo chủ sở hữu
    List<Camping> findByOwner(User owner);

    // Tìm các khu cắm trại có sẵn
    List<Camping> findByIsAvailableTrue();

    // Tìm theo rating
    List<Camping> findByRatingGreaterThanEqual(Double rating);

    // Tìm theo facilities
    @Query("SELECT DISTINCT c FROM Camping c JOIN c.facilities f WHERE f IN :facilities")
    List<Camping> findByFacilitiesIn(@Param("facilities") List<String> facilities);

    // Tìm kiếm tổng hợp
    @Query("SELECT c FROM Camping c WHERE " +
           "(:location IS NULL OR LOWER(c.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:minPrice IS NULL OR c.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR c.price <= :maxPrice) AND " +
           "(:minPlaces IS NULL OR c.maxPlaces >= :minPlaces) AND " +
           "(:isAvailable IS NULL OR c.isAvailable = :isAvailable)")
    List<Camping> searchCampings(
            @Param("location") String location,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minPlaces") Integer minPlaces,
            @Param("isAvailable") Boolean isAvailable);

    // Top rated campings
    List<Camping> findTop10ByOrderByRatingDesc();

    // Most booked campings
    List<Camping> findTop10ByOrderByBookingCountDesc();

    // Recently added campings
    List<Camping> findTop10ByOrderByCreatedAtDesc();

    Page<Camping> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Camping> findByMaxPlacesGreaterThanEqual(int places, Pageable pageable);
    Page<Camping> findByIsAvailable(boolean isAvailable, Pageable pageable);
}