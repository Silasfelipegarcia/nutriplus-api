package br.com.nutriplus.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "plan_day")
public class PlanDayEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID planId;
    private Integer dayIndex;
    @Lob
    private String mealsJson;
    @Lob
    private String dailySummary;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public Integer getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(Integer dayIndex) {
        this.dayIndex = dayIndex;
    }

    public String getMealsJson() {
        return mealsJson;
    }

    public void setMealsJson(String mealsJson) {
        this.mealsJson = mealsJson;
    }

    public String getDailySummary() {
        return dailySummary;
    }

    public void setDailySummary(String dailySummary) {
        this.dailySummary = dailySummary;
    }
}