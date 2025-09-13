package br.com.nutriplus.domain.port.out;

import br.com.nutriplus.domain.model.Adherence;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface AdherenceRepository {
    Optional<Adherence> findByUserAndDate(UUID userId, LocalDate date);

    Adherence save(Adherence adherence);
}