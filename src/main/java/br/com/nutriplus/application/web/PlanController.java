package br.com.nutriplus.application.web;

import br.com.nutriplus.application.service.PlanService;
import br.com.nutriplus.domain.model.Plan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    /**
     * Obtém o plano atual de um usuário.
     * GET /plans/users/{userId}/current
     */
    @GetMapping("/users/{userId}/current")
    public ResponseEntity<Plan> getCurrentPlan(@PathVariable("userId") UUID userId) {
        Plan currentPlan = planService.get(userId);
        return ResponseEntity.ok(currentPlan);
    }

    /**
     * Gera um novo plano para o usuário.
     * POST /plans/users/{userId}/generate
     */
    @PostMapping("/users/{userId}/generate")
    public ResponseEntity<Map<String, Object>> generatePlan(@PathVariable("userId") UUID userId) {
        int versionNumber = planService.generate(userId);
        return ResponseEntity.accepted().body(Map.of("version", versionNumber));
    }
}