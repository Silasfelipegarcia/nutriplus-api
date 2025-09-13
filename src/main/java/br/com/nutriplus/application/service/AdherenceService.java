package br.com.nutriplus.application.service;

import br.com.nutriplus.domain.model.Adherence;
import br.com.nutriplus.domain.port.in.MarkAdherenceUseCase;
import br.com.nutriplus.domain.port.out.AdherenceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdherenceService implements MarkAdherenceUseCase {
    private final AdherenceRepository adherenceRepository;

    public AdherenceService(AdherenceRepository adherenceRepository) {
        this.adherenceRepository = adherenceRepository;
    }

    @Override
    public void mark(UUID userId, LocalDate date, boolean mealBreakfast, boolean mealLunch, boolean mealDinner, boolean workout, int waterMl) {
        Optional<Adherence> existing = adherenceRepository.findByUserAndDate(userId, date);
        Adherence base = existing.orElse(new Adherence(UUID.randomUUID(), userId, date, false, false, false, false, null));
        Adherence updated = new Adherence(base.getId(), userId, date, mealBreakfast, mealLunch, mealDinner, workout, waterMl);
        adherenceRepository.save(updated);
    }
}