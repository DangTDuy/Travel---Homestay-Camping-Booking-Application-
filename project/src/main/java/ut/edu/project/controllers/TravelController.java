package ut.edu.project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Travel;
import ut.edu.project.services.TravelService;

import java.util.List;

@Controller
@RequestMapping("/travels") // Đường dẫn chính của trang du lịch
public class TravelController {

    @Autowired
    private TravelService travelService;

    @GetMapping
    public String showTravelList(Model model) {
        List<Travel> travels = travelService.getAllTravels(); // Lấy danh sách tour du lịch
        model.addAttribute("travels", travels);
        model.addAttribute("currentPage", "travels");
        return "travel/travel"; // Điều hướng đến file templates/travel/travel.html
    }

    // Add new method for travel details
    @GetMapping("/{id}")
    public String getTravelById(@PathVariable Long id, Model model) {
        Travel travel = travelService.getTravelById(id)
                .orElseThrow(() -> new RuntimeException("Travel not found with id: " + id)); // Or handle appropriately
        model.addAttribute("travel", travel);
        model.addAttribute("currentPage", "travels");
        // Optionally add CSRF token if needed for forms on the detail page later
        // if (csrfToken != null) {
        //     model.addAttribute("_csrf", csrfToken);
        // }
        return "travel/travel-detail"; // Point to the new detail template
    }
    @PostMapping("/create")
    public String createTravel(@Valid @ModelAttribute Travel travel, BindingResult result, Model model) {
        if (result.hasErrors()) {
            // Trả lại form nếu có lỗi
            return "travel/form";
        }
        travelService.createTravel(travel);
        return "redirect:/travels";
    }

}
