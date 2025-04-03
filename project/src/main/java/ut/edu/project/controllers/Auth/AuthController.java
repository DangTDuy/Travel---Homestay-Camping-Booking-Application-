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

@Controller
@RequestMapping("/auth")
public class AuthController {

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
        if (result.contains("thành công")) { // Sửa lại điều kiện này
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Bạn có thể đăng nhập ngay.");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
            redirectAttributes.addFlashAttribute("registerDTO", registerDTO);
        }
        return "redirect:/auth/register-user";
    }

    @GetMapping("/login-user")
    public String showLoginForm(
            @RequestParam(value = "registerSuccess", required = false) Boolean registerSuccess,
            @RequestParam(value = "error", required = false) String error,
            Model model) {

        model.addAttribute("loginRequest", new LoginRequest());

        if (registerSuccess != null && registerSuccess) {
            model.addAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Đăng nhập thất bại");
        }

        return "auth/login";
    }
}