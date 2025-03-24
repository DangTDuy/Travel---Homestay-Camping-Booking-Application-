package ut.edu.project.services;

import org.springframework.stereotype.Service;
import ut.edu.project.models.Homestay;
import ut.edu.project.repositories.HomestayRepository;

import java.util.List;
import java.util.Optional;

@Service
public class HomestayServiceImpl implements HomestayService {

    private final HomestayRepository homestayRepository;

    // Constructor Injection
    public HomestayServiceImpl(HomestayRepository homestayRepository) {
        this.homestayRepository = homestayRepository;
    }

    @Override
    public List<Homestay> getAllHomestays() {
        return homestayRepository.findAll();
    }

    @Override
    public Optional<Homestay> getHomestayById(Long id) {
        return homestayRepository.findById(id);
    }

    @Override
    public Homestay saveHomestay(Homestay homestay) {
        return homestayRepository.save(homestay);
    }

    @Override
    public void deleteHomestay(Long id) {
        homestayRepository.deleteById(id);
    }
}
