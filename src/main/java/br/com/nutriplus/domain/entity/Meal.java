package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.MealType;
import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "meals")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    protected Meal() {
    }

    private Meal(Builder builder) {
        this.id = builder.id;
        this.mealPlan = builder.mealPlan;
        this.mealType = builder.mealType;
        this.name = builder.name;
        this.sortOrder = builder.sortOrder != null ? builder.sortOrder : 0;
        this.scheduledTime = builder.scheduledTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MealPlan getMealPlan() {
        return mealPlan;
    }

    public void setMealPlan(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public static class Builder {
        private Long id;
        private MealPlan mealPlan;
        private MealType mealType;
        private String name;
        private Integer sortOrder;
        private LocalTime scheduledTime;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder mealPlan(MealPlan mealPlan) {
            this.mealPlan = mealPlan;
            return this;
        }

        public Builder mealType(MealType mealType) {
            this.mealType = mealType;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder sortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder scheduledTime(LocalTime scheduledTime) {
            this.scheduledTime = scheduledTime;
            return this;
        }

        public Meal build() {
            return new Meal(this);
        }
    }
}
