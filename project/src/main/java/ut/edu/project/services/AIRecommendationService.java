package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.project.models.Homestay;
import ut.edu.project.models.User;
import ut.edu.project.repositories.HomestayRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIRecommendationService {

    private final HomestayRepository homestayRepository;
    private final UserService userService;

    @Autowired
    public AIRecommendationService(HomestayRepository homestayRepository, UserService userService) {
        this.homestayRepository = homestayRepository;
        this.userService = userService;
    }

    public List<Homestay> recommendHomestays(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lấy tất cả homestay (sau này có thể thêm logic AI phức tạp hơn)
        List<Homestay> allHomestays = homestayRepository.findAll();

        // Hiện tại chỉ shuffle ngẫu nhiên, sau này có thể thêm logic phân tích sở thích
        Collections.shuffle(allHomestays);

        // Trả về 3 homestay ngẫu nhiên
        return allHomestays.stream().limit(3).collect(Collectors.toList());
    }
}