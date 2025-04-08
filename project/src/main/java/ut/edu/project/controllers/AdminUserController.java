package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ut.edu.project.services.UserService;
import java.util.List;
import ut.edu.project.dtos.UserDTO;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    // Hiển thị danh sách users
    @GetMapping
    public String getAllUsers(Model model) {
        List<UserDTO> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("currentPage", "admin/users");
        return "admin/admin-user";
    }

    // Hiển thị form tạo user mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new UserDTO());
        model.addAttribute("currentPage", "admin/users");
        return "admin/admin-user-form";
    }

    // Xử lý tạo user mới
    @PostMapping("/create")
    public String createUser(@ModelAttribute UserDTO userDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.saveUser(userDTO);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
            return "redirect:/admin/users";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create user");
            return "redirect:/admin/users/create";
        }
    }

    // Hiển thị form chỉnh sửa user
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            UserDTO user = userService.getUserById(id);
            model.addAttribute("user", user);
            model.addAttribute("currentPage", "admin/users");
            return "admin/admin-user-form";
        } catch (RuntimeException e) {
            return "redirect:/admin/users?error=user_not_found";
        }
    }

    // Xử lý cập nhật user
    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute UserDTO userDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            userDTO.setId(id);
            userService.saveUser(userDTO);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
            return "redirect:/admin/users";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user");
            return "redirect:/admin/users/" + id + "/edit";
        }
    }

    // Xem chi tiết user
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        try {
            UserDTO user = userService.getUserById(id);
            model.addAttribute("user", user);
            model.addAttribute("currentPage", "admin/users");
            return "admin/admin-user-detail";
        } catch (RuntimeException e) {
            return "redirect:/admin/users?error=user_not_found";
        }
    }

    // Xóa user
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete user");
        }
        return "redirect:/admin/users";
    }
}