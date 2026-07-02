package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.AcceptTermsRequest;
import br.com.nutriplus.dto.request.ChangePasswordRequest;
import br.com.nutriplus.dto.request.DeleteAccountRequest;
import br.com.nutriplus.dto.request.UpdateUserProfileRequest;
import br.com.nutriplus.dto.response.AuthResponse;
import br.com.nutriplus.dto.response.UserResponse;
import br.com.nutriplus.infrastructure.security.WebPortalClientVerifier;
import br.com.nutriplus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;
    private final WebPortalClientVerifier webPortalClientVerifier;

    public UserController(UserService userService, WebPortalClientVerifier webPortalClientVerifier) {
        this.userService = userService;
        this.webPortalClientVerifier = webPortalClientVerifier;
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

    @PostMapping("/me/accept-terms")
    public UserResponse acceptTerms(@Valid @RequestBody AcceptTermsRequest request) {
        return userService.acceptTerms(request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@Valid @RequestBody DeleteAccountRequest request, HttpServletRequest httpRequest) {
        webPortalClientVerifier.requireWebPortal(httpRequest);
        userService.deleteAccount(request);
    }
}
