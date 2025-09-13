package br.com.nutriplus.infrastructure.persistence.jpa.adapter;

import br.com.nutriplus.domain.model.Plan;
import br.com.nutriplus.domain.model.PlanDay;
import br.com.nutriplus.domain.port.out.PlanRepository;
import br.com.nutriplus.infrastructure.persistence.jpa.PlanDayJpa;
import br.com.nutriplus.infrastructure.persistence.jpa.PlanJpa;
import br.com.nutriplus.infrastructure.persistence.jpa.entity.PlanDayEntity;
import br.com.nutriplus.infrastructure.persistence.jpa.entity.PlanEntity;
import br.com.nutriplus.infrastructure.persistence.jpa.mapper.PlanMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PlanRepositoryAdapter implements PlanRepository {
    private final PlanJpa planJpa;
    private final PlanDayJpa planDayJpa;

    public PlanRepositoryAdapter(PlanJpa planJpa, PlanDayJpa planDayJpa) {
        this.planJpa = planJpa;
        this.planDayJpa = planDayJpa;
    }

    @Override
    public Plan savePlanHeader(Plan plan) {
        PlanEntity planEntity = PlanMapper.toPlanEntity(plan);
        PlanEntity saved = planJpa.save(planEntity);
        return PlanMapper.toPlanDomain(saved, java.util.List.of());
    }

    @Override
    public void saveDays(List<PlanDay> days) {
        List<PlanDayEntity> entities = days.stream().map(PlanMapper::toDayEntity).collect(Collectors.toList());
        planDayJpa.saveAll(entities);
    }

    @Override
    public Optional<Plan> findLatestPlan(UUID userId) {
        List<PlanEntity> plans = planJpa.findByUserIdOrderByVersionNumberDesc(userId);
        if (plans.isEmpty()) return Optional.empty();
        PlanEntity planEntity = plans.get(0);
        List<PlanDay> dayDomains = planDayJpa.findByPlanIdOrderByDayIndexAsc(planEntity.getId()).stream().map(PlanMapper::toDayDomain).collect(Collectors.toList());
        return Optional.of(PlanMapper.toPlanDomain(planEntity, dayDomains));
    }

    @Override
    public List<PlanDay> findDays(UUID planId) {
        return planDayJpa.findByPlanIdOrderByDayIndexAsc(planId).stream().map(PlanMapper::toDayDomain).collect(Collectors.toList());
    }

    @Override
    public int latestVersionNumber(UUID userId) {
        List<PlanEntity> plans = planJpa.findByUserIdOrderByVersionNumberDesc(userId);
        return plans.isEmpty() ? 0 : plans.get(0).getVersionNumber();
    }
}