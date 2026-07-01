package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "meal_items")
public class MealItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @Column(name = "food_name", nullable = false, length = 200)
    private String foodName;

    @Column(name = "quantity_g", nullable = false, precision = 8, scale = 2)
    private BigDecimal quantityG;

    @Column(name = "quantity_display", length = 80)
    private String quantityDisplay;

    @Column(name = "unit_kind", length = 20)
    private String unitKind;

    @Column(precision = 8, scale = 2)
    private BigDecimal calories;

    @Column(name = "protein_g", precision = 8, scale = 2)
    private BigDecimal proteinG;

    @Column(name = "carbs_g", precision = 8, scale = 2)
    private BigDecimal carbsG;

    @Column(name = "fat_g", precision = 8, scale = 2)
    private BigDecimal fatG;

    protected MealItem() {
    }

    private MealItem(Builder builder) {
        this.id = builder.id;
        this.meal = builder.meal;
        this.foodName = builder.foodName;
        this.quantityG = builder.quantityG;
        this.quantityDisplay = builder.quantityDisplay;
        this.unitKind = builder.unitKind;
        this.calories = builder.calories;
        this.proteinG = builder.proteinG;
        this.carbsG = builder.carbsG;
        this.fatG = builder.fatG;
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

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public BigDecimal getQuantityG() {
        return quantityG;
    }

    public void setQuantityG(BigDecimal quantityG) {
        this.quantityG = quantityG;
    }

    public String getQuantityDisplay() {
        return quantityDisplay;
    }

    public void setQuantityDisplay(String quantityDisplay) {
        this.quantityDisplay = quantityDisplay;
    }

    public String getUnitKind() {
        return unitKind;
    }

    public void setUnitKind(String unitKind) {
        this.unitKind = unitKind;
    }

    public BigDecimal getCalories() {
        return calories;
    }

    public void setCalories(BigDecimal calories) {
        this.calories = calories;
    }

    public BigDecimal getProteinG() {
        return proteinG;
    }

    public void setProteinG(BigDecimal proteinG) {
        this.proteinG = proteinG;
    }

    public BigDecimal getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(BigDecimal carbsG) {
        this.carbsG = carbsG;
    }

    public BigDecimal getFatG() {
        return fatG;
    }

    public void setFatG(BigDecimal fatG) {
        this.fatG = fatG;
    }

    public static class Builder {
        private Long id;
        private Meal meal;
        private String foodName;
        private BigDecimal quantityG;
        private String quantityDisplay;
        private String unitKind;
        private BigDecimal calories;
        private BigDecimal proteinG;
        private BigDecimal carbsG;
        private BigDecimal fatG;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder meal(Meal meal) {
            this.meal = meal;
            return this;
        }

        public Builder foodName(String foodName) {
            this.foodName = foodName;
            return this;
        }

        public Builder quantityG(BigDecimal quantityG) {
            this.quantityG = quantityG;
            return this;
        }

        public Builder quantityDisplay(String quantityDisplay) {
            this.quantityDisplay = quantityDisplay;
            return this;
        }

        public Builder unitKind(String unitKind) {
            this.unitKind = unitKind;
            return this;
        }

        public Builder calories(BigDecimal calories) {
            this.calories = calories;
            return this;
        }

        public Builder proteinG(BigDecimal proteinG) {
            this.proteinG = proteinG;
            return this;
        }

        public Builder carbsG(BigDecimal carbsG) {
            this.carbsG = carbsG;
            return this;
        }

        public Builder fatG(BigDecimal fatG) {
            this.fatG = fatG;
            return this;
        }

        public MealItem build() {
            return new MealItem(this);
        }
    }
}
