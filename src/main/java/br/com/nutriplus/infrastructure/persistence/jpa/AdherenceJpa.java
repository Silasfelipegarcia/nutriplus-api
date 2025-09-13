package br.com.nutriplus.infrastructure.persistence.jpa;

import br.com.nutriplus.infrastructure.persistence.jpa.entity.AdherenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface AdherenceJpa extends JpaRepository<AdherenceEntity, UUID> {
    Optional<AdherenceEntity> findByUserIdAndDate(UUID userId, LocalDate date);
}