package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.dtos.UserDTO;
import ut.edu.project.dtos.UpdateUserDTO;
import ut.edu.project.services.UserService;
import ut.edu.project.models.User;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        Optional<UserDTO> userDTO = userService.findByUsername(username).map(user -> new UserDTO(
                user.getId(), // Thêm id
                user.getUsername(),
                user.getHoTen(),
                user.getEmail(),
                user.getSoDienThoai(),
                user.getRole()
        ));
        return userDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        UserDTO userDTO = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/me/update")
    public ResponseEntity<UserDTO> updateCurrentUser(Authentication authentication, @RequestBody UpdateUserDTO updatedUserDTO) {
        String username = authentication.getName();
        UserDTO userDTO = userService.updateUserByUsername(username, updatedUserDTO);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }
}