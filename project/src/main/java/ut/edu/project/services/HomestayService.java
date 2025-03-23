package ut.edu.project.services;

import ut.edu.project.models.Homestay;

import java.util.List;
import java.util.Optional;

public interface HomestayService {
    List<Homestay> getAllHomestays();
    Optional<Homestay> getHomestayById(Long id);
    Homestay saveHomestay(Homestay homestay);
    void deleteHomestay(Long id);
}
