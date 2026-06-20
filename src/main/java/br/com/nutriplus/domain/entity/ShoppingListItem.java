package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "shopping_list_items")
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(nullable = false, length = 100)
    private String quantity;

    @Column(length = 100)
    private String category;

    @Column(name = "food_type", length = 64)
    private String foodType;

    @Column(name = "protein_leanness", length = 32)
    private String proteinLeanness;

    @Column(name = "kcal_estimate")
    private Integer kcalEstimate;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "alternatives_json", columnDefinition = "json")
    private String alternativesJson;

    protected ShoppingListItem() {
    }

    private ShoppingListItem(Builder builder) {
        this.id = builder.id;
        this.shoppingList = builder.shoppingList;
        this.itemName = builder.itemName;
        this.quantity = builder.quantity;
        this.category = builder.category;
        this.foodType = builder.foodType;
        this.proteinLeanness = builder.proteinLeanness;
        this.kcalEstimate = builder.kcalEstimate;
        this.explanation = builder.explanation;
        this.alternativesJson = builder.alternativesJson;
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

    public ShoppingList getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getProteinLeanness() {
        return proteinLeanness;
    }

    public void setProteinLeanness(String proteinLeanness) {
        this.proteinLeanness = proteinLeanness;
    }

    public Integer getKcalEstimate() {
        return kcalEstimate;
    }

    public void setKcalEstimate(Integer kcalEstimate) {
        this.kcalEstimate = kcalEstimate;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getAlternativesJson() {
        return alternativesJson;
    }

    public void setAlternativesJson(String alternativesJson) {
        this.alternativesJson = alternativesJson;
    }

    public static class Builder {
        private Long id;
        private ShoppingList shoppingList;
        private String itemName;
        private String quantity;
        private String category;
        private String foodType;
        private String proteinLeanness;
        private Integer kcalEstimate;
        private String explanation;
        private String alternativesJson;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder shoppingList(ShoppingList shoppingList) {
            this.shoppingList = shoppingList;
            return this;
        }

        public Builder itemName(String itemName) {
            this.itemName = itemName;
            return this;
        }

        public Builder quantity(String quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder foodType(String foodType) {
            this.foodType = foodType;
            return this;
        }

        public Builder proteinLeanness(String proteinLeanness) {
            this.proteinLeanness = proteinLeanness;
            return this;
        }

        public Builder kcalEstimate(Integer kcalEstimate) {
            this.kcalEstimate = kcalEstimate;
            return this;
        }

        public Builder explanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        public Builder alternativesJson(String alternativesJson) {
            this.alternativesJson = alternativesJson;
            return this;
        }

        public ShoppingListItem build() {
            return new ShoppingListItem(this);
        }
    }
}
