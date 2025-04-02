package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Camping;
import ut.edu.project.services.CampingService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/camping")
public class CampingController {
    @Autowired
    private CampingService campingService;

    //  HIỂN THỊ GIAO DIỆN THYMELEAF VỚI TÌM KIẾM, LỌC, SẮP XẾP VÀ PHÂN TRANG
    @GetMapping
    public String showCampingList(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Integer minPlaces,
            @RequestParam(required = false) String availability, // Lọc theo trạng thái khả dụng
            @RequestParam(required = false) String sort,         // Sắp xếp
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {
        // Thêm các tham số vào model để Thymeleaf sử dụng
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("minPlaces", minPlaces);
        model.addAttribute("availability", availability);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);

        // Lấy danh sách camping theo điều kiện
        Page<Camping> campingPage;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            campingPage = campingService.searchCampingsByName(searchTerm, page, size, sort);
        } else if (minPlaces != null) {
            campingPage = campingService.getCampingsByMinPlaces(minPlaces, page, size, sort);
        } else if (availability != null && !availability.isEmpty()) {
            campingPage = campingService.getCampingsByAvailability(Boolean.parseBoolean(availability), page, size, sort);
        } else {
            campingPage = campingService.getCampingsPaginated(page, size, sort);
        }

        // Thêm dữ liệu vào model
        model.addAttribute("campings", campingPage.getContent());
        model.addAttribute("totalPages", campingPage.getTotalPages());

        return "camping"; // Trả về file templates/camping.html
    }

    // API: LẤY DANH SÁCH CAMPING
    @GetMapping("/api")
    @ResponseBody
    public List<Camping> getAllCampings() {
        return campingService.getAllCampings();
    }

    // API: LẤY CAMPING THEO ID
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Camping> getCampingById(@PathVariable Long id) {
        Optional<Camping> camping = campingService.getCampingById(id);
        return camping.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // API: THÊM CAMPING
    @PostMapping("/api")
    @ResponseBody
    public Camping addCamping(@RequestBody Camping camping) {
        return campingService.addCamping(camping);
    }

    // API: CẬP NHẬT CAMPING
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Camping> updateCamping(@PathVariable Long id, @RequestBody Camping campingDetails) {
        Camping updatedCamping = campingService.updateCamping(id, campingDetails);
        return updatedCamping != null ? ResponseEntity.ok(updatedCamping) : ResponseEntity.notFound().build();
    }

    // API: XÓA CAMPING
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCamping(@PathVariable Long id) {
        campingService.deleteCamping(id);
        return ResponseEntity.noContent().build();
    }

    // API: LẤY DANH SÁCH CAMPING CÒN CHỖ
    @GetMapping("/api/available")
    @ResponseBody
    public List<Camping> getAvailableCampings() {
        return campingService.getAvailableCampings();
    }

    // API: ĐẶT CHỖ CAMPING
    @PostMapping("/book/{id}")
    public String bookCamping(@PathVariable Long id, Model model) {
        boolean booked = campingService.bookCamping(id);
        if (booked) {
            model.addAttribute("message", "Đặt chỗ thành công!");
        } else {
            model.addAttribute("message", "Không thể đặt chỗ, có thể đã hết chỗ!");
        }
        // Sau khi đặt chỗ, quay lại trang danh sách
        return "redirect:/camping";
    }
}