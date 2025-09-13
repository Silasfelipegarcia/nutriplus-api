package br.com.nutriplus.application.web;
import br.com.nutriplus.application.service.AdherenceService; import br.com.nutriplus.application.web.dto.AdherenceDTO; import jakarta.validation.Valid; import jakarta.validation.constraints.NotNull; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.time.LocalDate; import java.util.UUID;
@RestController public class AdherenceController {
  private final AdherenceService adherenceService;
  public AdherenceController(AdherenceService adherenceService){ this.adherenceService=adherenceService; }
  @PostMapping("/adherence/daily") public ResponseEntity<Void> mark(@RequestParam @NotNull UUID userId, @RequestBody @Valid AdherenceDTO request){
    adherenceService.mark(userId, LocalDate.parse(request.date()), request.mealBreakfast(), request.mealLunch(), request.mealDinner(), request.workout(), request.waterMl());
    return ResponseEntity.noContent().build();
  }
}