package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Travel;
import ut.edu.project.services.TravelService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/travel")
public class TravelController {

    @Autowired
    private TravelService travelService;

    @GetMapping
    public List<Travel> getAllTravels() {
        return travelService.getAllTravels();
    }

    @GetMapping("/{id}")
    public Optional<Travel> getTravelById(@PathVariable Long id) {
        return travelService.getTravelById(id);
    }

    @PostMapping
    public Travel createTravel(@RequestBody Travel travel) {
        return travelService.createTravel(travel);
    }

    @DeleteMapping("/{id}")
    public void deleteTravel(@PathVariable Long id) {
        travelService.deleteTravel(id);
    }
}
