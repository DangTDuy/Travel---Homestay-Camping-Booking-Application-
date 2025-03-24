package ut.edu.project.controllers;

import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Homestay;
import ut.edu.project.services.HomestayService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/homestays")
public class HomestayController {

    private final HomestayService homestayService;

    public HomestayController(HomestayService homestayService) {
        this.homestayService = homestayService;
    }

    @GetMapping
    public List<Homestay> getAllHomestays() {
        return homestayService.getAllHomestays();
    }

    @GetMapping("/{id}")
    public Optional<Homestay> getHomestayById(@PathVariable Long id) {
        return homestayService.getHomestayById(id);
    }

    @PostMapping
    public Homestay createHomestay(@RequestBody Homestay homestay) {
        return homestayService.saveHomestay(homestay);
    }

    @PutMapping("/{id}")
    public Homestay updateHomestay(@PathVariable Long id, @RequestBody Homestay homestay) {
        homestay.setId(id);
        return homestayService.saveHomestay(homestay);
    }

    @DeleteMapping("/{id}")
    public void deleteHomestay(@PathVariable Long id) {
        homestayService.deleteHomestay(id);
    }
}