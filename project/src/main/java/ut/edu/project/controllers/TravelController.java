package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.models.Travel;
import ut.edu.project.services.TravelService;

import java.util.List;

@RestController
@RequestMapping("/travels")
public class TravelController {
    @Autowired
    private TravelService travelService;

    @GetMapping
    public List<Travel> getAllTravels() {
        return travelService.getAllTravels();
    }

    @PostMapping
    public Travel createTravel(@RequestBody Travel travel) {
        return travelService.createTravel(travel);
    }

}
