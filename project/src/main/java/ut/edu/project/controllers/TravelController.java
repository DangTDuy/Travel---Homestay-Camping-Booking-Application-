package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
        return "travel"; // Điều hướng đến file templates/travel.html
    }
}
