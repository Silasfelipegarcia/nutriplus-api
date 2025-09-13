package br.com.nutriplus.infrastructure.persistence.jpa.adapter;

import br.com.nutriplus.domain.model.Goal;
import br.com.nutriplus.domain.port.out.GoalRepository;
import br.com.nutriplus.infrastructure.persistence.jpa.GoalJpa;
import br.com.nutriplus.infrastructure.persistence.jpa.entity.GoalEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GoalRepositoryAdapter implements GoalRepository {
    private final GoalJpa goalJpa;

    public GoalRepositoryAdapter(GoalJpa goalJpa) {
        this.goalJpa = goalJpa;
    }

    @Override
    public Goal save(Goal goal) {
        GoalEntity entity = new GoalEntity();
        entity.setId(goal.getId());
        entity.setUserId(goal.getUserId());
        entity.setType(goal.getType());
        entity.setPace(goal.getPace());
        entity.setTargetWeightKg(goal.getTargetWeightKg());
        entity.setTargetBfPercent(goal.getTargetBfPercent());
        entity.setCreatedAt(goal.getCreatedAt());
        goalJpa.save(entity);
        return goal;
    }

    @Override
    public List<Goal> findByUser(UUID userId) {
        return goalJpa.findByUserIdOrderByCreatedAtDesc(userId).stream().map(e -> new Goal(e.getId(), e.getUserId(), e.getType(), e.getPace(), e.getTargetWeightKg(), e.getTargetBfPercent(), e.getCreatedAt())).collect(Collectors.toList());
    }
}