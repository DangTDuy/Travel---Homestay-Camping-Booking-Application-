    package ut.edu.project.controllers;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.web.csrf.CsrfToken;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;
    import ut.edu.project.models.Homestay;
    import ut.edu.project.services.HomestayService;

    import java.util.List;

    @Controller
    @RequestMapping("/homestay")
    public class HomestayController {

        @Autowired
        private HomestayService homestayService;

        @GetMapping
        public String getAllHomestays(Model model, @RequestParam(required = false) String location) {
            List<Homestay> homestays = homestayService.searchHomestays(location);
            model.addAttribute("homestays", homestays);
            return "homestay/homestay"; // Đường dẫn chính xác đến homestay.html
        }


            // làm tiếp cái này nhe Nhựt

    //    @GetMapping("/{id}/book")
    //    public String redirectToBookingForm(@PathVariable Long id) {
    //        return "redirect:/bookings/new?homestayId=" + id; // Chuyển hướng sang trang booking
    //    }


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
    }