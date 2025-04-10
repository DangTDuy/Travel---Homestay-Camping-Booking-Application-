package ut.edu.project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.dtos.AdditionalDTO;

import ut.edu.project.services.AdditionalService;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class AdditionalServiceApiController {

    private final AdditionalService additionalService;

    @GetMapping("/{id}")
    public ResponseEntity<AdditionalDTO> getService(@PathVariable Long id) {
        try {
            AdditionalDTO service = additionalService.getAdditionalById(id);
            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<AdditionalDTO> createService(@RequestBody AdditionalDTO serviceDTO) {
        AdditionalDTO createdService = additionalService.createAdditional(serviceDTO);
        return ResponseEntity.ok(createdService);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdditionalDTO> updateService(
            @PathVariable Long id, @RequestBody AdditionalDTO serviceDTO) {
        try {
            AdditionalDTO updatedService = additionalService.updateAdditional(id, serviceDTO);
            return ResponseEntity.ok(updatedService);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        additionalService.deleteAdditional(id);
        return ResponseEntity.ok().build();
    }
}
