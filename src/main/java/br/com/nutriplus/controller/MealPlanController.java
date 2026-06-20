package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.MealPlanGenerationStatusResponse;
import br.com.nutriplus.dto.response.MealPlanResponse;
import br.com.nutriplus.service.MealPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MealPlanGenerationStatusResponse generate() {
        return mealPlanService.enqueueGeneration();
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
