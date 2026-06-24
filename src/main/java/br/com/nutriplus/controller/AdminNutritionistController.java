package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.NutritionistPendingResponse;
import br.com.nutriplus.service.AdminNutritionistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/nutritionists")
public class AdminNutritionistController {

    private final AdminNutritionistService adminNutritionistService;

    public AdminNutritionistController(AdminNutritionistService adminNutritionistService) {
        this.adminNutritionistService = adminNutritionistService;
    }

    @GetMapping("/pending")
    public List<NutritionistPendingResponse> pending() {
        return adminNutritionistService.listPendingVerification();
    }

    @PostMapping("/{id}/verify")
    public void verify(@PathVariable Long id) {
        adminNutritionistService.verify(id);
    }

    @PostMapping("/{id}/reject")
    public void reject(@PathVariable Long id) {
        adminNutritionistService.reject(id);
    }
}
