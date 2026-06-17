package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_profiles")
public class NutritionProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    @Column(name = "height_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "current_weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal currentWeightKg;

    @Column(name = "target_weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetWeightKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Goal goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_preference", nullable = false)
    private DietaryPreference dietaryPreference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Restriction restriction;

    @Column(name = "bmr_kcal", precision = 8, scale = 2)
    private BigDecimal bmrKcal;

    @Column(name = "tdee_kcal", precision = 8, scale = 2)
    private BigDecimal tdeeKcal;

    @Column(name = "target_calories", precision = 8, scale = 2)
    private BigDecimal targetCalories;

    @Column(name = "target_protein_g", precision = 8, scale = 2)
    private BigDecimal targetProteinG;

    @Column(name = "target_carbs_g", precision = 8, scale = 2)
    private BigDecimal targetCarbsG;

    @Column(name = "target_fat_g", precision = 8, scale = 2)
    private BigDecimal targetFatG;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected NutritionProfile() {
    }

    private NutritionProfile(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.age = builder.age;
        this.sex = builder.sex;
        this.heightCm = builder.heightCm;
        this.currentWeightKg = builder.currentWeightKg;
        this.targetWeightKg = builder.targetWeightKg;
        this.goal = builder.goal;
        this.activityLevel = builder.activityLevel;
        this.dietaryPreference = builder.dietaryPreference;
        this.restriction = builder.restriction;
        this.bmrKcal = builder.bmrKcal;
        this.tdeeKcal = builder.tdeeKcal;
        this.targetCalories = builder.targetCalories;
        this.targetProteinG = builder.targetProteinG;
        this.targetCarbsG = builder.targetCarbsG;
        this.targetFatG = builder.targetFatG;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public BigDecimal getCurrentWeightKg() {
        return currentWeightKg;
    }

    public void setCurrentWeightKg(BigDecimal currentWeightKg) {
        this.currentWeightKg = currentWeightKg;
    }

    public BigDecimal getTargetWeightKg() {
        return targetWeightKg;
    }

    public void setTargetWeightKg(BigDecimal targetWeightKg) {
        this.targetWeightKg = targetWeightKg;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public DietaryPreference getDietaryPreference() {
        return dietaryPreference;
    }

    public void setDietaryPreference(DietaryPreference dietaryPreference) {
        this.dietaryPreference = dietaryPreference;
    }

    public Restriction getRestriction() {
        return restriction;
    }

    public void setRestriction(Restriction restriction) {
        this.restriction = restriction;
    }

    public BigDecimal getBmrKcal() {
        return bmrKcal;
    }

    public void setBmrKcal(BigDecimal bmrKcal) {
        this.bmrKcal = bmrKcal;
    }

    public BigDecimal getTdeeKcal() {
        return tdeeKcal;
    }

    public void setTdeeKcal(BigDecimal tdeeKcal) {
        this.tdeeKcal = tdeeKcal;
    }

    public BigDecimal getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(BigDecimal targetCalories) {
        this.targetCalories = targetCalories;
    }

    public BigDecimal getTargetProteinG() {
        return targetProteinG;
    }

    public void setTargetProteinG(BigDecimal targetProteinG) {
        this.targetProteinG = targetProteinG;
    }

    public BigDecimal getTargetCarbsG() {
        return targetCarbsG;
    }

    public void setTargetCarbsG(BigDecimal targetCarbsG) {
        this.targetCarbsG = targetCarbsG;
    }

    public BigDecimal getTargetFatG() {
        return targetFatG;
    }

    public void setTargetFatG(BigDecimal targetFatG) {
        this.targetFatG = targetFatG;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private Long id;
        private User user;
        private Integer age;
        private Sex sex;
        private BigDecimal heightCm;
        private BigDecimal currentWeightKg;
        private BigDecimal targetWeightKg;
        private Goal goal;
        private ActivityLevel activityLevel;
        private DietaryPreference dietaryPreference;
        private Restriction restriction;
        private BigDecimal bmrKcal;
        private BigDecimal tdeeKcal;
        private BigDecimal targetCalories;
        private BigDecimal targetProteinG;
        private BigDecimal targetCarbsG;
        private BigDecimal targetFatG;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder age(Integer age) {
            this.age = age;
            return this;
        }

        public Builder sex(Sex sex) {
            this.sex = sex;
            return this;
        }

        public Builder heightCm(BigDecimal heightCm) {
            this.heightCm = heightCm;
            return this;
        }

        public Builder currentWeightKg(BigDecimal currentWeightKg) {
            this.currentWeightKg = currentWeightKg;
            return this;
        }

        public Builder targetWeightKg(BigDecimal targetWeightKg) {
            this.targetWeightKg = targetWeightKg;
            return this;
        }

        public Builder goal(Goal goal) {
            this.goal = goal;
            return this;
        }

        public Builder activityLevel(ActivityLevel activityLevel) {
            this.activityLevel = activityLevel;
            return this;
        }

        public Builder dietaryPreference(DietaryPreference dietaryPreference) {
            this.dietaryPreference = dietaryPreference;
            return this;
        }

        public Builder restriction(Restriction restriction) {
            this.restriction = restriction;
            return this;
        }

        public Builder bmrKcal(BigDecimal bmrKcal) {
            this.bmrKcal = bmrKcal;
            return this;
        }

        public Builder tdeeKcal(BigDecimal tdeeKcal) {
            this.tdeeKcal = tdeeKcal;
            return this;
        }

        public Builder targetCalories(BigDecimal targetCalories) {
            this.targetCalories = targetCalories;
            return this;
        }

        public Builder targetProteinG(BigDecimal targetProteinG) {
            this.targetProteinG = targetProteinG;
            return this;
        }

        public Builder targetCarbsG(BigDecimal targetCarbsG) {
            this.targetCarbsG = targetCarbsG;
            return this;
        }

        public Builder targetFatG(BigDecimal targetFatG) {
            this.targetFatG = targetFatG;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public NutritionProfile build() {
            return new NutritionProfile(this);
        }
    }
}
