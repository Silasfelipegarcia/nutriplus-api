package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;

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

    protected ShoppingListItem() {
    }

    private ShoppingListItem(Builder builder) {
        this.id = builder.id;
        this.shoppingList = builder.shoppingList;
        this.itemName = builder.itemName;
        this.quantity = builder.quantity;
        this.category = builder.category;
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

    public static class Builder {
        private Long id;
        private ShoppingList shoppingList;
        private String itemName;
        private String quantity;
        private String category;

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

        public ShoppingListItem build() {
            return new ShoppingListItem(this);
        }
    }
}
