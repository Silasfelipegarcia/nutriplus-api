package br.com.nutriplus.infrastructure.persistence.jpa.adapter;

import br.com.nutriplus.domain.model.Adherence;
import br.com.nutriplus.domain.port.out.AdherenceRepository;
import br.com.nutriplus.infrastructure.persistence.jpa.AdherenceJpa;
import br.com.nutriplus.infrastructure.persistence.jpa.entity.AdherenceEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public class AdherenceRepositoryAdapter implements AdherenceRepository {
    private final AdherenceJpa adherenceJpa;

    public AdherenceRepositoryAdapter(AdherenceJpa adherenceJpa) {
        this.adherenceJpa = adherenceJpa;
    }

    @Override
    public Optional<Adherence> findByUserAndDate(UUID userId, LocalDate date) {
        return adherenceJpa.findByUserIdAndDate(userId, date).map(e -> new Adherence(e.getId(), e.getUserId(), e.getDate(), Boolean.TRUE.equals(e.getMealBreakfast()), Boolean.TRUE.equals(e.getMealLunch()), Boolean.TRUE.equals(e.getMealDinner()), Boolean.TRUE.equals(e.getWorkout()), e.getWaterMl()));
    }

    @Override
    public Adherence save(Adherence adherence) {
        AdherenceEntity entity = new AdherenceEntity();
        entity.setId(adherence.getId());
        entity.setUserId(adherence.getUserId());
        entity.setDate(adherence.getDate());
        entity.setMealBreakfast(adherence.isMealBreakfast());
        entity.setMealLunch(adherence.isMealLunch());
        entity.setMealDinner(adherence.isMealDinner());
        entity.setWorkout(adherence.isWorkout());
        entity.setWaterMl(adherence.getWaterMl());
        adherenceJpa.save(entity);
        return adherence;
    }
}