package br.com.nutriplus.application.web;
import br.com.nutriplus.application.service.BioService; import br.com.nutriplus.application.web.dto.BioManualDTO; import jakarta.validation.Valid; import jakarta.validation.constraints.NotNull; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.time.LocalDate; import java.util.UUID;
@RestController public class BioController {
  private final BioService bioService; public BioController(BioService bioService){ this.bioService=bioService; }
  @PostMapping("/bio/manual") public ResponseEntity<Void> manual(@RequestParam @NotNull UUID userId, @RequestBody @Valid BioManualDTO dto){
    bioService.insert(userId, LocalDate.parse(dto.reportDate()), dto.weightKg(), dto.bodyFatPercent(), dto.skeletalMuscleMassKg(), dto.phaseAngleDeg(), dto.notes());
    return ResponseEntity.noContent().build();
  }
}