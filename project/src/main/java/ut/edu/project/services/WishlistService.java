//package ut.edu.project.services;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import ut.edu.project.models.Homestay;
//import ut.edu.project.models.User;
//import ut.edu.project.repositories.HomestayRepository;
//import ut.edu.project.repositories.UserRepository;
//import java.util.Set;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import java.util.HashSet; // Import HashSet
//
//@Service
//public class WishlistService {
//
//    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private HomestayRepository homestayRepository;
//
//    @Transactional
//    public boolean addToWishlist(String username, Long homestayId) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found: " + username));
//        Homestay homestay = homestayRepository.findById(homestayId)
//                .orElseThrow(() -> new RuntimeException("Homestay not found: " + homestayId));
//
//        if (user.getWishlistHomestays() == null) { // Khởi tạo nếu null
//             user.setWishlistHomestays(new HashSet<>());
//        }
//
//        if (user.getWishlistHomestays().contains(homestay)) {
//            logger.info("Homestay {} already in wishlist for user {}", homestayId, username);
//            return false; // Already in wishlist
//        }
//
//        user.getWishlistHomestays().add(homestay);
//        // userRepository.save(user); // Lưu User sẽ tự cập nhật bảng join
//        logger.info("Added homestay {} to wishlist for user {}", homestayId, username);
//        return true;
//    }
//
//    @Transactional
//    public boolean removeFromWishlist(String username, Long homestayId) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found: " + username));
//        Homestay homestay = homestayRepository.findById(homestayId)
//                .orElseThrow(() -> new RuntimeException("Homestay not found: " + homestayId));
//
//        if (user.getWishlistHomestays() == null || !user.getWishlistHomestays().contains(homestay)) {
//             logger.info("Homestay {} not found in wishlist for user {}", homestayId, username);
//             return false; // Not in wishlist
//         }
//
//        boolean removed = user.getWishlistHomestays().remove(homestay);
//        if (removed) {
//            // userRepository.save(user); // Lưu User sẽ tự cập nhật bảng join
//            logger.info("Removed homestay {} from wishlist for user {}", homestayId, username);
//        }
//        return removed;
//    }
//
//    @Transactional(readOnly = true)
//    public Set<Homestay> getWishlist(String username) {
//        // Sử dụng query mới để fetch wishlist cùng lúc
//        User user = userRepository.findByUsernameWithWishlist(username)
//                .orElseThrow(() -> new RuntimeException("User not found: " + username));
//
//        Set<Homestay> wishlist = user.getWishlistHomestays();
//        // Không cần gọi size() nữa vì đã được fetch
//        if (wishlist == null) {
//            wishlist = new HashSet<>(); // Đảm bảo không trả về null
//        }
//        logger.info("Fetched wishlist for user {} with size {}", username, wishlist.size());
//        return wishlist;
//    }
//
//    @Transactional(readOnly = true)
//    public boolean isInWishlist(String username, Long homestayId) {
//         User user = userRepository.findByUsername(username).orElse(null);
//         if (user == null || user.getWishlistHomestays() == null) {
//             return false;
//         }
//         // Kiểm tra hiệu quả hơn mà không cần tải toàn bộ collection
//         // Cách này vẫn có thể trigger lazy load, cần kiểm tra với JPA provider
//         return user.getWishlistHomestays().stream().anyMatch(h -> h.getId().equals(homestayId));
//    }
//}
