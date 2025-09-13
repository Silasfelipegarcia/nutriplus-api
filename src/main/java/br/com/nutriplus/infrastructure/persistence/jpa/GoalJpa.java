package br.com.nutriplus.infrastructure.persistence.jpa;

import br.com.nutriplus.infrastructure.persistence.jpa.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoalJpa extends JpaRepository<GoalEntity, UUID> {
    List<GoalEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}