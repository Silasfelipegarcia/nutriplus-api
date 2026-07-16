package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_food_extras")
public class DailyFoodExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "estimated_calories", nullable = false)
    private Integer estimatedCalories;

    @Column(name = "impact_message", columnDefinition = "TEXT")
    private String impactMessage;

    @Column(name = "estimated_carbs_g", precision = 8, scale = 1)
    private BigDecimal estimatedCarbsG;

    @Column(name = "meal_id")
    private Long mealId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected DailyFoodExtra() {
    }

    public DailyFoodExtra(User user, LocalDate entryDate, String description, int estimatedCalories,
                          BigDecimal estimatedCarbsG, String impactMessage, Long mealId) {
        this.user = user;
        this.entryDate = entryDate;
        this.description = description;
        this.estimatedCalories = estimatedCalories;
        this.estimatedCarbsG = estimatedCarbsG;
        this.impactMessage = impactMessage;
        this.mealId = mealId;
    }

    public DailyFoodExtra(User user, LocalDate entryDate, String description, int estimatedCalories,
                          BigDecimal estimatedCarbsG, String impactMessage) {
        this(user, entryDate, description, estimatedCalories, estimatedCarbsG, impactMessage, null);
    }

    public DailyFoodExtra(User user, LocalDate entryDate, String description, int estimatedCalories, String impactMessage) {
        this(user, entryDate, description, estimatedCalories, null, impactMessage, null);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public String getDescription() {
        return description;
    }

    public Integer getEstimatedCalories() {
        return estimatedCalories;
    }

    public String getImpactMessage() {
        return impactMessage;
    }

    public BigDecimal getEstimatedCarbsG() {
        return estimatedCarbsG;
    }

    public Long getMealId() {
        return mealId;
    }
}
