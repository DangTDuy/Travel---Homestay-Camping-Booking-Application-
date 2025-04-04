package ut.edu.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ut.edu.project.dtos.UserDTO;
import ut.edu.project.dtos.UpdateUserDTO;
import ut.edu.project.services.UserService;

import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        Optional<UserDTO> userDTO = userService.findByUsername(username)
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getHoTen(),
                        user.getEmail(),
                        user.getSoDienThoai(),
                        user.getRole()
                ));
        return userDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/profile/update")
    public ResponseEntity<UserDTO> updateCurrentUser(
            Authentication authentication,
            @RequestBody UpdateUserDTO updatedUserDTO
    ) {
        String username = authentication.getName();
        UserDTO userDTO = userService.updateUserByUsername(username, updatedUserDTO);
        return ResponseEntity.ok(userDTO);
    }
}