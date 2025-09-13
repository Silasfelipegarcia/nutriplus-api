package br.com.nutriplus.domain.port.out;

import br.com.nutriplus.domain.model.Plan;
import br.com.nutriplus.domain.model.PlanDay;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepository {
    Plan savePlanHeader(Plan plan);

    void saveDays(List<PlanDay> days);

    Optional<Plan> findLatestPlan(UUID userId);

    List<PlanDay> findDays(UUID planId);

    int latestVersionNumber(UUID userId);
}