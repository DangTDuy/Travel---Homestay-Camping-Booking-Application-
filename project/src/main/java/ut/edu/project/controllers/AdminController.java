package ut.edu.project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Homestay;
import ut.edu.project.services.HomestayService;

import java.util.List;

@Controller
@RequestMapping("/admin/homestay")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    @Autowired
    private HomestayService homestayService;

    @GetMapping
    public String adminHomestayPage(Model model, @RequestParam(required = false) String location) {
        List<Homestay> homestays = homestayService.searchHomestays(location);
        model.addAttribute("homestays", homestays);
        return "admin-homestay";
    }

    @PostMapping("/create")
    public ResponseEntity<Homestay> createHomestay(
            @Valid @RequestBody Homestay homestay,
            Authentication authentication
    ) {
        String username = authentication.getName();
        Homestay createdHomestay = homestayService.createHomestay(homestay, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHomestay);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Homestay> updateHomestay(
            @PathVariable Long id,
            @Valid @RequestBody Homestay homestay,
            Authentication authentication
    ) {
        String username = authentication.getName();
        Homestay updatedHomestay = homestayService.updateHomestay(id, homestay, username);
        return ResponseEntity.ok(updatedHomestay);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHomestay(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        homestayService.deleteHomestay(id, username);
        return ResponseEntity.noContent().build();
    }
}