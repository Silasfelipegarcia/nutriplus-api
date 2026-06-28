package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.MealPlanGenerateRequest;
import br.com.nutriplus.dto.response.MealPlanGenerationStatusResponse;
import br.com.nutriplus.dto.response.MealPlanResponse;
import br.com.nutriplus.dto.response.PlanRegenerationEligibilityResponse;
import br.com.nutriplus.service.MealPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @GetMapping("/regeneration-eligibility")
    public PlanRegenerationEligibilityResponse regenerationEligibility() {
        return mealPlanService.getRegenerationEligibility();
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MealPlanGenerationStatusResponse generate(@Valid @RequestBody MealPlanGenerateRequest request) {
        return mealPlanService.enqueueGeneration(request);
    }

    @GetMapping("/generation-status")
    public MealPlanGenerationStatusResponse getGenerationStatus() {
        return mealPlanService.getGenerationStatus();
    }

    @GetMapping("/latest")
    public MealPlanResponse getLatest() {
        return mealPlanService.getLatest();
    }
}
