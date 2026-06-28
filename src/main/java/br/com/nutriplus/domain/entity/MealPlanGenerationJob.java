package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.MealPlanGenerationStatus;
import br.com.nutriplus.domain.enums.PlanRegenerationReason;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "meal_plan_generation_jobs")
public class MealPlanGenerationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealPlanGenerationStatus status = MealPlanGenerationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id")
    private MealPlan mealPlan;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "regeneration_reason", length = 40)
    private PlanRegenerationReason regenerationReason;

    @Column(name = "progress_review_id")
    private Long progressReviewId;

    protected MealPlanGenerationJob() {
    }

    private MealPlanGenerationJob(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.status = builder.status != null ? builder.status : MealPlanGenerationStatus.PENDING;
        this.mealPlan = builder.mealPlan;
        this.errorMessage = builder.errorMessage;
        this.createdAt = builder.createdAt;
        this.startedAt = builder.startedAt;
        this.completedAt = builder.completedAt;
        this.regenerationReason = builder.regenerationReason;
        this.progressReviewId = builder.progressReviewId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public MealPlanGenerationStatus getStatus() {
        return status;
    }

    public void setStatus(MealPlanGenerationStatus status) {
        this.status = status;
    }

    public MealPlan getMealPlan() {
        return mealPlan;
    }

    public void setMealPlan(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public PlanRegenerationReason getRegenerationReason() {
        return regenerationReason;
    }

    public void setRegenerationReason(PlanRegenerationReason regenerationReason) {
        this.regenerationReason = regenerationReason;
    }

    public Long getProgressReviewId() {
        return progressReviewId;
    }

    public void setProgressReviewId(Long progressReviewId) {
        this.progressReviewId = progressReviewId;
    }

    public static final class Builder {
        private Long id;
        private User user;
        private MealPlanGenerationStatus status;
        private MealPlan mealPlan;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private PlanRegenerationReason regenerationReason;
        private Long progressReviewId;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder status(MealPlanGenerationStatus status) {
            this.status = status;
            return this;
        }

        public Builder mealPlan(MealPlan mealPlan) {
            this.mealPlan = mealPlan;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder startedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder regenerationReason(PlanRegenerationReason regenerationReason) {
            this.regenerationReason = regenerationReason;
            return this;
        }

        public Builder progressReviewId(Long progressReviewId) {
            this.progressReviewId = progressReviewId;
            return this;
        }

        public MealPlanGenerationJob build() {
            return new MealPlanGenerationJob(this);
        }
    }
}
