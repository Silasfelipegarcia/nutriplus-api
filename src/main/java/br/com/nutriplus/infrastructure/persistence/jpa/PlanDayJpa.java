package br.com.nutriplus.infrastructure.persistence.jpa;

import br.com.nutriplus.infrastructure.persistence.jpa.entity.PlanDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanDayJpa extends JpaRepository<PlanDayEntity, UUID> {
    List<PlanDayEntity> findByPlanIdOrderByDayIndexAsc(UUID planId);
}