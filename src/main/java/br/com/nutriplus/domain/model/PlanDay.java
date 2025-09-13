package br.com.nutriplus.domain.model;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PlanDay {
    private final UUID id;
    private final UUID planId;
    private final Integer dayIndex;
    private final Map<String, Object> meals;
    private final String dailySummary;

    public PlanDay(UUID id, UUID planId, Integer dayIndex, Map<String, Object> meals, String dailySummary) {
        this.id = Objects.requireNonNull(id);
        this.planId = Objects.requireNonNull(planId);
        this.dayIndex = Objects.requireNonNull(dayIndex);
        this.meals = meals;
        this.dailySummary = dailySummary;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlanId() {
        return planId;
    }

    public Integer getDayIndex() {
        return dayIndex;
    }

    public Map<String, Object> getMeals() {
        return meals;
    }

    public String getDailySummary() {
        return dailySummary;
    }
}
