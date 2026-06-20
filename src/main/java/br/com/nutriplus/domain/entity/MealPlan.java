package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.PlanSource;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "medical_review_status", length = 20)
    private String medicalReviewStatus;

    @Column(name = "medical_review_notes", columnDefinition = "TEXT")
    private String medicalReviewNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_source", nullable = false)
    private PlanSource planSource = PlanSource.AI_ONLY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutritionist_id")
    private Nutritionist nutritionist;

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
        this.medicalReviewStatus = builder.medicalReviewStatus;
        this.medicalReviewNotes = builder.medicalReviewNotes;
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

    public String getMedicalReviewStatus() {
        return medicalReviewStatus;
    }

    public void setMedicalReviewStatus(String medicalReviewStatus) {
        this.medicalReviewStatus = medicalReviewStatus;
    }

    public String getMedicalReviewNotes() {
        return medicalReviewNotes;
    }

    public void setMedicalReviewNotes(String medicalReviewNotes) {
        this.medicalReviewNotes = medicalReviewNotes;
    }

    public PlanSource getPlanSource() {
        return planSource;
    }

    public void setPlanSource(PlanSource planSource) {
        this.planSource = planSource;
    }

    public Nutritionist getNutritionist() {
        return nutritionist;
    }

    public void setNutritionist(Nutritionist nutritionist) {
        this.nutritionist = nutritionist;
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
        private String medicalReviewStatus;
        private String medicalReviewNotes;
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

        public Builder medicalReviewStatus(String medicalReviewStatus) {
            this.medicalReviewStatus = medicalReviewStatus;
            return this;
        }

        public Builder medicalReviewNotes(String medicalReviewNotes) {
            this.medicalReviewNotes = medicalReviewNotes;
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
