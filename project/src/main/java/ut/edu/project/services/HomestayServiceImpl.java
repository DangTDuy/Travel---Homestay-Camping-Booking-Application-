package ut.edu.project.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Homestay;
import ut.edu.project.repositories.HomestayRepository;
import ut.edu.project.services.HomestayService;

import java.util.List;
import java.util.Optional;

@Service
public class HomestayServiceImpl implements HomestayService {

    @Autowired
    private HomestayRepository homestayRepository;

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
