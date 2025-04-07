package ut.edu.project.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import ut.edu.project.services.AdditionalService;
import ut.edu.project.dtos.AdditionalDTO;
import java.util.List;

@Controller
@RequestMapping("/additional")
public class AdditionalController {
    @Autowired
    private AdditionalService additionalService;

    @GetMapping
    public String listAdditionals(Model model) {
        List<AdditionalDTO> additionals = additionalService.getAllAdditionals();
        model.addAttribute("additionals", additionals);
        return "additional/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("additional", new AdditionalDTO());
        return "additional/form";
    }

    @PostMapping("/create")
    public String createAdditional(@ModelAttribute AdditionalDTO additional) {
        additionalService.createAdditional(additional);
        return "redirect:/additional";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        AdditionalDTO additional = additionalService.getAdditionalById(id);
        model.addAttribute("additional", additional);
        return "additional/form";
    }

    @PostMapping("/edit/{id}")
    public String updateAdditional(@PathVariable Long id, @ModelAttribute AdditionalDTO additional) {
        additionalService.updateAdditional(id, additional);
        return "redirect:/additional";
    }

    @PostMapping("/delete/{id}")
    public String deleteAdditional(@PathVariable Long id) {
        additionalService.deleteAdditional(id);
        return "redirect:/additional";
    }
} 