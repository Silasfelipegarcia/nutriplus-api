package br.com.nutriplus.infrastructure.persistence.jpa.mapper;

import br.com.nutriplus.domain.model.Plan;
import br.com.nutriplus.domain.model.PlanDay;
import br.com.nutriplus.infrastructure.persistence.jpa.entity.PlanDayEntity;
import br.com.nutriplus.infrastructure.persistence.jpa.entity.PlanEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PlanMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private PlanMapper() {
    }

    public static PlanEntity toPlanEntity(Plan domain) {
        PlanEntity entity = new PlanEntity();
        entity.setId(domain.getId() == null ? UUID.randomUUID() : domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setVersionNumber(domain.getVersionNumber());
        entity.setStartDate(domain.getStartDate());
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }

    public static Plan toPlanDomain(PlanEntity entity, List<PlanDay> days) {
        return new Plan(entity.getId(), entity.getUserId(), entity.getVersionNumber(), entity.getStartDate(), days);
    }

    public static PlanDayEntity toDayEntity(PlanDay domain) {
        try {
            PlanDayEntity entity = new PlanDayEntity();
            entity.setId(domain.getId() == null ? UUID.randomUUID() : domain.getId());
            entity.setPlanId(domain.getPlanId());
            entity.setDayIndex(domain.getDayIndex());
            entity.setMealsJson(objectMapper.writeValueAsString(domain.getMeals()));
            entity.setDailySummary(domain.getDailySummary());
            return entity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static PlanDay toDayDomain(PlanDayEntity entity) {
        try {
            Map<String, Object> meals = objectMapper.readValue(entity.getMealsJson(), new TypeReference<Map<String, Object>>() {
            });
            return new PlanDay(entity.getId(), entity.getPlanId(), entity.getDayIndex(), meals, entity.getDailySummary());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}