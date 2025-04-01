package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Camping;
import ut.edu.project.services.CampingService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/camping")
public class CampingController {
    @Autowired
    private CampingService campingService;

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
    public Camping addCamping(@RequestBody Camping camping) {
        return campingService.addCamping(camping);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Camping> updateCamping(@PathVariable Long id, @RequestBody Camping campingDetails) {
        Camping updatedCamping = campingService.updateCamping(id, campingDetails);
        return updatedCamping != null ? ResponseEntity.ok(updatedCamping) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCamping(@PathVariable Long id) {
        campingService.deleteCamping(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public List<Camping> getAvailableCampings() {
        return campingService.getAvailableCampings();
    }
}
