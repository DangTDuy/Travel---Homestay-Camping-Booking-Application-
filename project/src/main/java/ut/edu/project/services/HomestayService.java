package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;
import ut.edu.project.repositories.HomestayRepository;

import java.util.List;
import java.util.Optional;

@Service
public class HomestayService {

    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private UserService userService;

    public Homestay createHomestay(Homestay homestay, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().equals("ADMIN")) {
            throw new RuntimeException("Bạn không có quyền tạo homestay");
        }

        homestay.setOwner(user);
        return homestayRepository.save(homestay);
    }

    public List<Homestay> searchHomestays(String location) {
        if (location != null && !location.isEmpty()) {
            return homestayRepository.findByLocationContainingIgnoreCase(location);
        }
        return homestayRepository.findAll();
    }

    public Optional<Homestay> getHomestayById(Long id) {
        return homestayRepository.findById(id);
    }

    public List<Homestay> getHomestaysByOwner(String username) {
        User owner = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return homestayRepository.findByOwner(owner);
    }

    public Homestay updateHomestay(Long id, Homestay updatedHomestay, String username) {
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Homestay not found"));

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().equals("ADMIN")) {
            throw new RuntimeException("Chỉ ADMIN mới có quyền cập nhật homestay");
        }

        homestay.setName(updatedHomestay.getName());
        homestay.setLocation(updatedHomestay.getLocation());
        homestay.setDescription(updatedHomestay.getDescription());
        homestay.setPrice(updatedHomestay.getPrice()); // Đổi tên thành price
        homestay.setCapacity(updatedHomestay.getCapacity());
        homestay.setAmenities(updatedHomestay.getAmenities());
        homestay.setImageUrls(updatedHomestay.getImageUrls());
        return homestayRepository.save(homestay);
    }

    public void deleteHomestay(Long id, String username) {
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Homestay not found"));

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().equals("ADMIN")) {
            throw new RuntimeException("Chỉ ADMIN mới có quyền xóa homestay");
        }

        homestayRepository.deleteById(id);
    }
}