package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.ApplyMealItemSubstitutionRequest;
import br.com.nutriplus.dto.request.MealPlanGenerateRequest;
import br.com.nutriplus.dto.response.MealItemSubstitutionOptionsResponse;
import br.com.nutriplus.dto.response.MealPlanGenerationStatusResponse;
import br.com.nutriplus.dto.response.MealPlanResponse;
import br.com.nutriplus.dto.response.PlanRegenerationEligibilityResponse;
import br.com.nutriplus.service.MealPlanService;
import br.com.nutriplus.service.MealSubstitutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;
    private final MealSubstitutionService mealSubstitutionService;

    public MealPlanController(MealPlanService mealPlanService,
                              MealSubstitutionService mealSubstitutionService) {
        this.mealPlanService = mealPlanService;
        this.mealSubstitutionService = mealSubstitutionService;
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

    @GetMapping("/meals/{mealId}/items/{itemId}/substitutions")
    public MealItemSubstitutionOptionsResponse suggestSubstitutions(@PathVariable Long mealId,
                                                                    @PathVariable Long itemId) {
        return mealSubstitutionService.suggestSubstitutions(mealId, itemId);
    }

    @PostMapping("/meals/{mealId}/items/{itemId}/substitute")
    public MealPlanResponse applySubstitution(@PathVariable Long mealId,
                                              @PathVariable Long itemId,
                                              @Valid @RequestBody ApplyMealItemSubstitutionRequest request) {
        return mealSubstitutionService.applySubstitution(mealId, itemId, request);
    }
}
