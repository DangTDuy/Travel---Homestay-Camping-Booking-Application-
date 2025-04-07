package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ut.edu.project.models.Homestay;
import ut.edu.project.services.HomestayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/homestay")
public class HomestayController {

    private static final Logger log = LoggerFactory.getLogger(HomestayController.class);

    @Autowired
    private HomestayService homestayService;

    @GetMapping
    public String getAllHomestays(Model model, 
                                @RequestParam(required = false) String location,
                                @RequestParam(required = false) String priceRange,
                                @RequestParam(required = false) String rating) {
        try {
            log.info("Getting all homestays for user with filters - location: {}, priceRange: {}, rating: {}", 
                    location, priceRange, rating);
            List<Homestay> homestays = homestayService.searchHomestays(location, priceRange, rating);
            log.info("Found {} homestays", homestays.size());
            
            model.addAttribute("homestays", homestays);
            model.addAttribute("location", location);
            model.addAttribute("priceRange", priceRange);
            model.addAttribute("rating", rating);
            return "homestay/homestay";
        } catch (Exception e) {
            log.error("Error getting homestays: {}", e.getMessage(), e);
            model.addAttribute("error", "Không thể tải danh sách homestay: " + e.getMessage());
            return "homestay/homestay";
        }
    }

    @GetMapping("/{id}")
    public String getHomestayById(@PathVariable Long id, Model model, CsrfToken csrfToken) {
        Homestay homestay = homestayService.getHomestayById(id)
                .orElseThrow(() -> new RuntimeException("Homestay not found"));
        model.addAttribute("homestay", homestay);
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "homestay/homestay-detail";
    }

    @GetMapping("/{id}/book")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String bookHomestay(@PathVariable Long id, Model model) {
        Homestay homestay = homestayService.getHomestayById(id)
                .orElseThrow(() -> new RuntimeException("Homestay not found"));
        model.addAttribute("homestay", homestay);
        return "booking/booking-form";
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String uploadImages(@PathVariable Long id, 
                             @RequestParam("images") MultipartFile[] images) throws IOException {
        homestayService.uploadImages(id, images);
        return "redirect:/homestay/" + id;
    }
}