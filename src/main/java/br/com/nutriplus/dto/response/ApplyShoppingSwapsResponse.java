package br.com.nutriplus.dto.response;

public record ApplyShoppingSwapsResponse(
        ShoppingListResponse shoppingList,
        Long mealPlanId
) {
}
