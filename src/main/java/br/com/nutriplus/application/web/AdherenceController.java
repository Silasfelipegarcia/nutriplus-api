package br.com.nutriplus.application.web;

import br.com.nutriplus.application.service.AdherenceService;
import br.com.nutriplus.application.web.dto.AdherenceDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/adherence")
public class AdherenceController {

    private final AdherenceService adherenceService;

    public AdherenceController(AdherenceService adherenceService) {
        this.adherenceService = adherenceService;
    }

    /**
     * Marca a adesão diária do usuário.
     * POST /adherence/users/{userId}/daily
     */
    @PostMapping("/users/{userId}/daily")
    public ResponseEntity<Void> markDaily(@PathVariable("userId") UUID userId,
                                          @RequestBody @Valid AdherenceDTO request) {
        adherenceService.mark(
                userId,
                LocalDate.parse(request.date()),
                request.mealBreakfast(),
                request.mealLunch(),
                request.mealDinner(),
                request.workout(),
                request.waterMl()
        );
        return ResponseEntity.noContent().build();
    }
}