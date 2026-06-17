package br.com.nutriplus.dto.response;

public record ShoppingListItemResponse(
        Long id,
        String itemName,
        String quantity,
        String category
) {
}
