package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Review;
import ut.edu.project.services.ReviewService;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Hiển thị danh sách đánh giá và form tạo mới
    @GetMapping
    public String showReviews(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        model.addAttribute("review", new Review()); // Đối tượng để bind với form
        return "review"; // Trả về template review.html
    }

    // Xử lý gửi đánh giá
    @PostMapping
    public String createReview(@ModelAttribute Review review, Model model) {
        try {
            reviewService.saveReview(review);
            return "redirect:/reviews"; // Chuyển hướng về danh sách sau khi lưu
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("reviews", reviewService.getAllReviews());
            return "review"; // Quay lại trang nếu có lỗi
        }
    }

    // Xóa đánh giá
    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
        } catch (IllegalArgumentException e) {
            // Có thể thêm xử lý lỗi nếu cần
        }
        return "redirect:/reviews"; // Chuyển hướng về danh sách
    }
}
