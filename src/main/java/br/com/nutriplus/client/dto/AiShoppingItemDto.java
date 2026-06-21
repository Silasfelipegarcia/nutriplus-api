package br.com.nutriplus.client.dto;

import java.util.List;

public record AiShoppingItemDto(
        String itemName,
        String quantity,
        String category,
        String foodType,
        String proteinLeanness,
        Integer kcalEstimate,
        String explanation,
        List<String> alternatives,
        String swapGroup,
        List<AiShoppingSwapOptionDto> swapOptions,
        List<String> marketTips,
        String defaultOptionId,
        String recommendedOptionId
) {
}
