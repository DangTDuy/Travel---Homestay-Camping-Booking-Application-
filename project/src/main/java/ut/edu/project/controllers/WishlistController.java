//package ut.edu.project.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import ut.edu.project.services.WishlistService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController // Dùng RestController vì đây là API
//@RequestMapping("/api/wishlist") // Đặt đường dẫn cơ sở là /api/wishlist
//@PreAuthorize("isAuthenticated()") // Yêu cầu người dùng phải đăng nhập
//public class WishlistController {
//
//    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);
//
//    @Autowired
//    private WishlistService wishlistService;
//
//    @PostMapping("/add/{homestayId}")
//    public ResponseEntity<?> addToWishlist(@PathVariable Long homestayId, Authentication authentication) {
//        String username = authentication.getName();
//        Map<String, Object> response = new HashMap<>();
//        try {
//            boolean added = wishlistService.addToWishlist(username, homestayId);
//            if (added) {
//                response.put("success", true);
//                response.put("message", "Added to wishlist");
//                logger.info("User {} added homestay {} to wishlist", username, homestayId);
//                return ResponseEntity.ok(response);
//            } else {
//                response.put("success", false);
//                response.put("message", "Already in wishlist");
//                 logger.info("Homestay {} was already in wishlist for user {}", homestayId, username);
//                return ResponseEntity.ok(response); // Vẫn trả về OK nhưng success=false
//            }
//        } catch (RuntimeException e) {
//             logger.error("Error adding homestay {} to wishlist for user {}: {}", homestayId, username, e.getMessage());
//            response.put("success", false);
//            response.put("message", e.getMessage());
//            return ResponseEntity.badRequest().body(response);
//        } catch (Exception e) {
//            logger.error("Unexpected error adding homestay {} to wishlist for user {}: {}", homestayId, username, e.getMessage(), e);
//            response.put("success", false);
//            response.put("message", "An unexpected error occurred.");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//    @PostMapping("/remove/{homestayId}")
//    public ResponseEntity<?> removeFromWishlist(@PathVariable Long homestayId, Authentication authentication) {
//        String username = authentication.getName();
//        Map<String, Object> response = new HashMap<>();
//        try {
//            boolean removed = wishlistService.removeFromWishlist(username, homestayId);
//             if (removed) {
//                 response.put("success", true);
//                 response.put("message", "Removed from wishlist");
//                 logger.info("User {} removed homestay {} from wishlist", username, homestayId);
//                 return ResponseEntity.ok(response);
//             } else {
//                 response.put("success", false);
//                 response.put("message", "Not found in wishlist");
//                 logger.info("Homestay {} was not in wishlist for user {}", homestayId, username);
//                 return ResponseEntity.ok(response); // Vẫn trả về OK nhưng success=false
//             }
//        } catch (RuntimeException e) {
//            logger.error("Error removing homestay {} from wishlist for user {}: {}", homestayId, username, e.getMessage());
//            response.put("success", false);
//            response.put("message", e.getMessage());
//            return ResponseEntity.badRequest().body(response);
//        } catch (Exception e) {
//             logger.error("Unexpected error removing homestay {} from wishlist for user {}: {}", homestayId, username, e.getMessage(), e);
//             response.put("success", false);
//             response.put("message", "An unexpected error occurred.");
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//         }
//    }
//}
