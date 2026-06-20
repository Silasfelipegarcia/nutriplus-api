package br.com.nutriplus.dto.response;

import java.util.List;

public record ShoppingListItemResponse(
        Long id,
        String itemName,
        String quantity,
        String category,
        String foodType,
        String proteinLeanness,
        Integer kcalEstimate,
        String explanation,
        List<String> alternatives
) {
}
