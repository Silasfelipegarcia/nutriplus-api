package br.com.nutriplus.infrastructure.persistence.jpa;

import br.com.nutriplus.infrastructure.persistence.jpa.entity.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanJpa extends JpaRepository<PlanEntity, UUID> {
    List<PlanEntity> findByUserIdOrderByVersionNumberDesc(UUID userId);
}