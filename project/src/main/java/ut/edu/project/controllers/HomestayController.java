package ut.edu.project.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Homestay;
import ut.edu.project.services.HomestayService;

import java.util.List;

@RestController
@RequestMapping("/homestays")
public class HomestayController {

    @Autowired
    private HomestayService homestayService;

    @GetMapping
    public ResponseEntity<List<Homestay>> getAllHomestays(
            @RequestParam(required = false) String location) {
        List<Homestay> homestays = homestayService.searchHomestays(location);
        return ResponseEntity.ok(homestays);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Homestay> getHomestayById(@PathVariable Long id) {
        Homestay homestay = homestayService.getHomestayById(id)
                .orElseThrow(() -> new RuntimeException("Homestay not found"));
        return ResponseEntity.ok(homestay);
    }

    @GetMapping("/my-homestays")
    public ResponseEntity<List<Homestay>> getMyHomestays(Authentication authentication) {
        String username = authentication.getName();
        List<Homestay> myHomestays = homestayService.getHomestaysByOwner(username);
        return ResponseEntity.ok(myHomestays);
    }

    @PostMapping("/create")
    public ResponseEntity<Homestay> createHomestay(
            @Valid @RequestBody Homestay homestay,
            Authentication authentication) {
        String username = authentication.getName();
        Homestay createdHomestay = homestayService.createHomestay(homestay, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHomestay);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Homestay> updateHomestay(
            @PathVariable Long id,
            @Valid @RequestBody Homestay homestay,
            Authentication authentication) {
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