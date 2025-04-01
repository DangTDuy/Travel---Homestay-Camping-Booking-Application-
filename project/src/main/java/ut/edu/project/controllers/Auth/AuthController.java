package ut.edu.project.controllers.Auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ut.edu.project.dtos.RegisterDTO;
import ut.edu.project.services.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/auth/register-user")
    public String registerUser(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/auth/register-user")
    public String postRegisterUser(@ModelAttribute RegisterDTO registerDTO, Model model) {
        String result = userService.registerUser(registerDTO);
        if (result.contains("successfully")) {
            return "redirect:/auth/login-user";
        } else {
            model.addAttribute("error", result);
            return "auth/register";
        }
    }

    @GetMapping("/auth/login-user")
    public String loginUser() {
        return "auth/login";
    }
}