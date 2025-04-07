package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ut.edu.project.models.Camping;
import ut.edu.project.services.CampingService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/camping")
public class CampingController {
    @Autowired
    private CampingService campingService;

    @GetMapping
    public String showCampingList(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Integer minPlaces,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("minPlaces", minPlaces);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);

        Page<Camping> campingPage;
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            campingPage = campingService.searchCampingsByName(searchTerm, page, size, sort);
        } else if (minPlaces != null) {
            campingPage = campingService.getCampingsByMinPlaces(minPlaces, page, size, sort);
        } else {
            campingPage = campingService.getCampingsPaginated(page, size, sort, start, end);
        }

        model.addAttribute("campings", campingPage.getContent());
        model.addAttribute("totalPages", campingPage.getTotalPages());
        return "camping";
    }

    @GetMapping("/api")
    @ResponseBody
    public List<Camping> getAllCampings() {
        return campingService.getAllCampings();
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Camping> getCampingById(@PathVariable Long id) {
        Optional<Camping> camping = campingService.getCampingById(id);
        return camping.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Camping> addCamping(@RequestBody Camping camping) {
        return ResponseEntity.ok(campingService.createCamping(camping));
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Camping> updateCamping(@PathVariable Long id, @RequestBody Camping campingDetails) {
        Camping updatedCamping = campingService.updateCamping(id, campingDetails);
        return updatedCamping != null ? ResponseEntity.ok(updatedCamping) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCamping(@PathVariable	Long id) {
        campingService.deleteCamping(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/book/{id}")
    public String bookCamping(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam int numberOfPeople,
            @RequestParam String customerName,
            RedirectAttributes redirectAttributes) {
        boolean booked = campingService.bookCamping(id, LocalDate.parse(startDate), LocalDate.parse(endDate), numberOfPeople, customerName);
        if (booked) {
            redirectAttributes.addFlashAttribute("message", "Đặt chỗ thành công!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không thể đặt chỗ, có thể đã hết chỗ hoặc thời gian không hợp lệ!");
        }
        return "redirect:/camping";
    }

    @PostMapping("/cancel/{id}")
    public String cancelBooking(
            @PathVariable Long id,
            @RequestParam int bookingIndex,
            RedirectAttributes redirectAttributes) {
        boolean cancelled = campingService.cancelBooking(id, bookingIndex);
        if (cancelled) {
            redirectAttributes.addFlashAttribute("message", "Hủy đặt chỗ thành công!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không thể hủy, đã quá thời hạn hoặc không tìm thấy đặt chỗ!");
        }
        return "redirect:/camping";
    }

    @PostMapping("/review/{id}")
    public String addReview(
            @PathVariable Long id,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam String reviewerName,
            RedirectAttributes redirectAttributes) {
        boolean reviewed = campingService.addReview(id, rating, comment, reviewerName);
        if (reviewed) {
            redirectAttributes.addFlashAttribute("message", "Đánh giá đã được gửi!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không thể gửi đánh giá!");
        }
        return "redirect:/camping";
    }
}