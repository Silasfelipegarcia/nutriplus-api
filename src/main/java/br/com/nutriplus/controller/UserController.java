package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.ChangePasswordRequest;
import br.com.nutriplus.dto.request.UpdateUserProfileRequest;
import br.com.nutriplus.dto.response.AuthResponse;
import br.com.nutriplus.dto.response.UserResponse;
import br.com.nutriplus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me() {
        return userService.getMe();
    }

    @PutMapping("/me")
    public UserResponse updateProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        return userService.updateProfile(request);
    }

    @PutMapping("/me/password")
    public AuthResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return userService.changePassword(request);
    }
}
