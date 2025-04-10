package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ut.edu.project.services.UserService;
import ut.edu.project.dtos.UserDTO;

import java.util.List;
import java.util.Map;

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

    // Xử lý tạo user mới (AJAX)
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO savedUser = userService.saveUser(userDTO);
            return ResponseEntity.ok(savedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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

    // Xử lý cập nhật user (AJAX)
    @PutMapping("/{id}/edit")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            userDTO.setId(id);
            UserDTO updatedUser = userService.saveUser(userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Xem chi tiết user (HTML view)
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

    // API để lấy thông tin user theo ID (JSON)
    @GetMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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