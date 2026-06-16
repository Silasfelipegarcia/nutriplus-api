package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.MealPlanResponse;
import br.com.nutriplus.service.MealPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meal-plans")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public MealPlanResponse generate() {
        return mealPlanService.generate();
    }

    @GetMapping("/latest")
    public MealPlanResponse getLatest() {
        return mealPlanService.getLatest();
    }
}
