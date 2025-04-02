package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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

    // 🖥️ HIỂN THỊ GIAO DIỆN THYMELEAF
    @GetMapping
    public String showCampingList(@RequestParam(required = false) String searchTerm, Model model) {
        // Thêm searchTerm vào model để Thymeleaf có thể sử dụng
        model.addAttribute("searchTerm", searchTerm);

        // Lấy danh sách camping và thêm vào model
        model.addAttribute("campings", campingService.getAllCampings());

        return "camping"; // Trả về file templates/camping.html
    }

    // 📝 API: LẤY DANH SÁCH CAMPING
    @GetMapping("/api")
    @ResponseBody
    public List<Camping> getAllCampings() {
        return campingService.getAllCampings();
    }

    // 🔍 API: LẤY CAMPING THEO ID
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Camping> getCampingById(@PathVariable Long id) {
        Optional<Camping> camping = campingService.getCampingById(id);
        return camping.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ➕ API: THÊM CAMPING
    @PostMapping("/api")
    @ResponseBody
    public Camping addCamping(@RequestBody Camping camping) {
        return campingService.addCamping(camping);
    }

    // 🔄 API: CẬP NHẬT CAMPING
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Camping> updateCamping(@PathVariable Long id, @RequestBody Camping campingDetails) {
        Camping updatedCamping = campingService.updateCamping(id, campingDetails);
        return updatedCamping != null ? ResponseEntity.ok(updatedCamping) : ResponseEntity.notFound().build();
    }

    // ❌ API: XÓA CAMPING
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCamping(@PathVariable Long id) {
        campingService.deleteCamping(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ API: LẤY DANH SÁCH CAMPING CÒN CHỖ
    @GetMapping("/api/available")
    @ResponseBody
    public List<Camping> getAvailableCampings() {
        return campingService.getAvailableCampings();
    }
}
