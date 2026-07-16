package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_item_swap_events")
public class MealItemSwapEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "meal_id", nullable = false)
    private Long mealId;

    @Column(name = "meal_item_id", nullable = false)
    private Long mealItemId;

    @Column(name = "from_food_name", nullable = false, length = 200)
    private String fromFoodName;

    @Column(name = "to_food_name", nullable = false, length = 200)
    private String toFoodName;

    @Column(name = "from_calories", precision = 8, scale = 2)
    private BigDecimal fromCalories;

    @Column(name = "to_calories", precision = 8, scale = 2)
    private BigDecimal toCalories;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected MealItemSwapEvent() {
    }

    public MealItemSwapEvent(User user,
                             Long mealId,
                             Long mealItemId,
                             String fromFoodName,
                             String toFoodName,
                             BigDecimal fromCalories,
                             BigDecimal toCalories) {
        this.user = user;
        this.mealId = mealId;
        this.mealItemId = mealItemId;
        this.fromFoodName = fromFoodName;
        this.toFoodName = toFoodName;
        this.fromCalories = fromCalories;
        this.toCalories = toCalories;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Long getMealId() {
        return mealId;
    }

    public Long getMealItemId() {
        return mealItemId;
    }

    public String getFromFoodName() {
        return fromFoodName;
    }

    public String getToFoodName() {
        return toFoodName;
    }

    public BigDecimal getFromCalories() {
        return fromCalories;
    }

    public BigDecimal getToCalories() {
        return toCalories;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
