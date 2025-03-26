package ut.edu.project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Camping;
import ut.edu.project.services.CampingService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/campings")
@RequiredArgsConstructor
public class CampingController {
    private final CampingService campingService;

    @GetMapping
    public List<Camping> getAllCampings() {
        return campingService.getAllCampings();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Camping> getCampingById(@PathVariable Long id) {
        Optional<Camping> camping = campingService.getCampingById(id);
        return camping.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Camping createCamping(@RequestBody Camping camping) {
        return campingService.saveCamping(camping);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCamping(@PathVariable Long id) {
        campingService.deleteCamping(id);
        return ResponseEntity.noContent().build();
    }
}