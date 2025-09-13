package br.com.nutriplus.application.web;
import br.com.nutriplus.application.service.PlanService; import br.com.nutriplus.domain.model.Plan; import jakarta.validation.constraints.NotNull; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.util.UUID;
@RestController public class PlanController {
  private final PlanService planService;
  public PlanController(PlanService planService){ this.planService=planService; }
  @GetMapping("/plan/current") public ResponseEntity<Plan> current(@RequestParam @NotNull UUID userId){ return ResponseEntity.ok(planService.get(userId)); }
  @PostMapping("/plan/generate") public ResponseEntity<?> generate(@RequestParam @NotNull UUID userId){ int version = planService.generate(userId); return ResponseEntity.accepted().body(java.util.Map.of("version", version)); }
}