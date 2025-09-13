package br.com.nutriplus.application.web;

import br.com.nutriplus.application.service.BioService;
import br.com.nutriplus.application.web.dto.BioManualDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/bio")
public class BioController {

    private final BioService bioService;

    public BioController(BioService bioService) {
        this.bioService = bioService;
    }

    /**
     * Insere métricas de bioimpedância manualmente.
     * POST /bio/users/{userId}/manual
     */
    @PostMapping("/users/{userId}/manual")
    public ResponseEntity<Void> insertManual(@PathVariable("userId") UUID userId,
                                             @RequestBody @Valid BioManualDTO dto) {
        bioService.insert(
                userId,
                LocalDate.parse(dto.reportDate()),
                dto.weightKg(),
                dto.bodyFatPercent(),
                dto.skeletalMuscleMassKg(),
                dto.phaseAngleDeg(),
                dto.notes()
        );
        return ResponseEntity.noContent().build();
    }
}