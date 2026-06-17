package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.NutritionProfileRequest;
import br.com.nutriplus.dto.response.NutritionProfileResponse;
import br.com.nutriplus.service.NutritionProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nutrition-profile")
public class NutritionProfileController {

    private final NutritionProfileService nutritionProfileService;

    public NutritionProfileController(NutritionProfileService nutritionProfileService) {
        this.nutritionProfileService = nutritionProfileService;
    }

    @PostMapping
    public NutritionProfileResponse save(@Valid @RequestBody NutritionProfileRequest request) {
        return nutritionProfileService.saveOrUpdate(request);
    }

    @GetMapping
    public NutritionProfileResponse get() {
        return nutritionProfileService.get();
    }
}
