package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Homestay;
import ut.edu.project.services.HomestayService;

import java.util.List;

@RestController
@RequestMapping("/homestay")
public class HomestayController {

    @Autowired
    private HomestayService homestayService;

    @GetMapping("/list")
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
}