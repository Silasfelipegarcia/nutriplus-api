package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.LoginRequest;
import br.com.nutriplus.dto.request.RefreshTokenRequest;
import br.com.nutriplus.dto.request.RegisterRequest;
import br.com.nutriplus.dto.request.NutritionistRegisterRequest;
import br.com.nutriplus.dto.response.AuthResponse;
import br.com.nutriplus.dto.response.RegisterResponse;
import br.com.nutriplus.service.AuthService;
import br.com.nutriplus.service.NutritionistProService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final NutritionistProService nutritionistProService;

    public AuthController(AuthService authService, NutritionistProService nutritionistProService) {
        this.authService = authService;
        this.nutritionistProService = nutritionistProService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/beta-request")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse betaRequest(@Valid @RequestBody RegisterRequest request) {
        return authService.betaRequest(request);
    }

    @PostMapping("/beta-request/nutritionist")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse betaRequestNutritionist(@Valid @RequestBody NutritionistRegisterRequest request) {
        return nutritionistProService.betaRequest(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }
}
