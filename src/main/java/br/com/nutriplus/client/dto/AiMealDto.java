package br.com.nutriplus.client.dto;

import java.util.List;

public record AiMealDto(
        String mealType,
        String name,
        Integer sortOrder,
        List<AiMealItemDto> items
) {
}
