package br.com.nutriplus.domain.port.out;

import br.com.nutriplus.domain.model.Goal;

import java.util.List;
import java.util.UUID;

public interface GoalRepository {
    Goal save(Goal goal);

    List<Goal> findByUser(UUID userId);
}