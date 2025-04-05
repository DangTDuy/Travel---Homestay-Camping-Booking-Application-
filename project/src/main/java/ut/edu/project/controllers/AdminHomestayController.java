package ut.edu.project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Homestay;
import ut.edu.project.services.HomestayService;

import java.util.List;

@Controller
@RequestMapping("/admin/homestay")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminHomestayController {

    @Autowired
    private HomestayService homestayService;

    @GetMapping
    public String adminHomestayPage(Model model, @RequestParam(required = false) String location) {
        List<Homestay> homestays = homestayService.searchHomestays(location);
        model.addAttribute("homestays", homestays);
        return "admin/admin-homestay";
    }

    @PostMapping("/create")
    public String createHomestay(
            @Valid @ModelAttribute Homestay homestay,
            BindingResult result,
            Authentication authentication,
            Model model) {
        if (result.hasErrors()) {
            List<Homestay> homestays = homestayService.searchHomestays(null);
            model.addAttribute("homestays", homestays);
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin nhập vào");
            return "admin/admin-homestay";
        }
        try {
            String username = authentication.getName();
            homestayService.createHomestay(homestay, username);
            return "redirect:/admin/homestay";
        } catch (RuntimeException e) {
            List<Homestay> homestays = homestayService.searchHomestays(null);
            model.addAttribute("homestays", homestays);
            model.addAttribute("error", e.getMessage());
            return "admin/admin-homestay";
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHomestay(
            @PathVariable Long id,
            @Valid @RequestBody Homestay homestay,
            BindingResult result,
            Authentication authentication) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        try {
            String username = authentication.getName();
            Homestay updatedHomestay = homestayService.updateHomestay(id, homestay, username);
            return ResponseEntity.ok(updatedHomestay);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHomestay(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            homestayService.deleteHomestay(id, username);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}