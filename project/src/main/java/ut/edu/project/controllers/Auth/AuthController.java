package ut.edu.project.controllers.Auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ut.edu.project.dtos.LoginRequest;
import ut.edu.project.dtos.RegisterDTO;
import ut.edu.project.services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/register-user")
    public String showRegisterForm(Model model) {
        // Chỉ thêm registerDTO nếu chưa có (tránh ghi đè khi redirect với lỗi)
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterDTO());
        }
        return "auth/register";
    }

    @PostMapping("/register-user")
    public String processRegistration(
            @Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerDTO", bindingResult);
            redirectAttributes.addFlashAttribute("registerDTO", registerDTO);
            return "redirect:/auth/register-user";
        }

        String result = userService.registerUser(registerDTO);
        if (result.contains("thành công")) {
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Bạn có thể đăng nhập ngay.");
            return "redirect:/auth/login-user";
        } else {
            redirectAttributes.addFlashAttribute("error", result);
            redirectAttributes.addFlashAttribute("registerDTO", registerDTO);
            return "redirect:/auth/register-user";
        }
    }

    @GetMapping("/login-user")
    public String showLoginForm(Model model) {
        logger.info("Showing login form");
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "auth/login";
    }
}