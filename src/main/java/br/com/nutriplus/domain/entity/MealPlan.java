package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_plans")
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_profile_id", nullable = false)
    private NutritionProfile nutritionProfile;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "total_calories", precision = 8, scale = 2)
    private BigDecimal totalCalories;

    @Column(name = "total_protein_g", precision = 8, scale = 2)
    private BigDecimal totalProteinG;

    @Column(name = "total_carbs_g", precision = 8, scale = 2)
    private BigDecimal totalCarbsG;

    @Column(name = "total_fat_g", precision = 8, scale = 2)
    private BigDecimal totalFatG;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String disclaimer;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<Meal> meals = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected MealPlan() {
    }

    private MealPlan(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.nutritionProfile = builder.nutritionProfile;
        this.planDate = builder.planDate;
        this.totalCalories = builder.totalCalories;
        this.totalProteinG = builder.totalProteinG;
        this.totalCarbsG = builder.totalCarbsG;
        this.totalFatG = builder.totalFatG;
        this.disclaimer = builder.disclaimer;
        this.aiModel = builder.aiModel;
        this.meals = builder.meals != null ? builder.meals : new ArrayList<>();
        this.createdAt = builder.createdAt;
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

    public NutritionProfile getNutritionProfile() {
        return nutritionProfile;
    }

    public void setNutritionProfile(NutritionProfile nutritionProfile) {
        this.nutritionProfile = nutritionProfile;
    }

    public LocalDate getPlanDate() {
        return planDate;
    }

    public void setPlanDate(LocalDate planDate) {
        this.planDate = planDate;
    }

    public BigDecimal getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(BigDecimal totalCalories) {
        this.totalCalories = totalCalories;
    }

    public BigDecimal getTotalProteinG() {
        return totalProteinG;
    }

    public void setTotalProteinG(BigDecimal totalProteinG) {
        this.totalProteinG = totalProteinG;
    }

    public BigDecimal getTotalCarbsG() {
        return totalCarbsG;
    }

    public void setTotalCarbsG(BigDecimal totalCarbsG) {
        this.totalCarbsG = totalCarbsG;
    }

    public BigDecimal getTotalFatG() {
        return totalFatG;
    }

    public void setTotalFatG(BigDecimal totalFatG) {
        this.totalFatG = totalFatG;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class Builder {
        private Long id;
        private User user;
        private NutritionProfile nutritionProfile;
        private LocalDate planDate;
        private BigDecimal totalCalories;
        private BigDecimal totalProteinG;
        private BigDecimal totalCarbsG;
        private BigDecimal totalFatG;
        private String disclaimer;
        private String aiModel;
        private List<Meal> meals;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder nutritionProfile(NutritionProfile nutritionProfile) {
            this.nutritionProfile = nutritionProfile;
            return this;
        }

        public Builder planDate(LocalDate planDate) {
            this.planDate = planDate;
            return this;
        }

        public Builder totalCalories(BigDecimal totalCalories) {
            this.totalCalories = totalCalories;
            return this;
        }

        public Builder totalProteinG(BigDecimal totalProteinG) {
            this.totalProteinG = totalProteinG;
            return this;
        }

        public Builder totalCarbsG(BigDecimal totalCarbsG) {
            this.totalCarbsG = totalCarbsG;
            return this;
        }

        public Builder totalFatG(BigDecimal totalFatG) {
            this.totalFatG = totalFatG;
            return this;
        }

        public Builder disclaimer(String disclaimer) {
            this.disclaimer = disclaimer;
            return this;
        }

        public Builder aiModel(String aiModel) {
            this.aiModel = aiModel;
            return this;
        }

        public Builder meals(List<Meal> meals) {
            this.meals = meals;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public MealPlan build() {
            return new MealPlan(this);
        }
    }
}
