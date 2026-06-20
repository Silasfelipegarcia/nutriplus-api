package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.TrainingProfileRequest;
import br.com.nutriplus.dto.response.NutritionProfileResponse;
import br.com.nutriplus.dto.response.SportCatalogItemResponse;
import br.com.nutriplus.dto.response.TrainingProfileResponse;
import br.com.nutriplus.service.TrainingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/training")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/sports")
    public List<SportCatalogItemResponse> sports() {
        return trainingService.getSportCatalog();
    }

    @GetMapping("/profile")
    public TrainingProfileResponse profile() {
        return trainingService.getProfile();
    }

    @PutMapping("/profile")
    public TrainingProfileResponse saveProfile(@Valid @RequestBody TrainingProfileRequest request) {
        return trainingService.saveProfile(request);
    }

    @PostMapping("/apply")
    public NutritionProfileResponse applyToPlan() {
        return trainingService.applyToPlan();
    }
}
