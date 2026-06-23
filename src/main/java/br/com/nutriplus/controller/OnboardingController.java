package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.OnboardingCompleteRequest;
import br.com.nutriplus.dto.response.NutritionProfileResponse;
import br.com.nutriplus.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/onboarding/complete")
    public NutritionProfileResponse complete(@Valid @RequestBody OnboardingCompleteRequest request) {
        return onboardingService.complete(request);
    }
}
