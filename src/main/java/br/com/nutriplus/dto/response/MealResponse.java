package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.MealType;

import java.util.List;

public record MealResponse(
        Long id,
        MealType mealType,
        String name,
        List<MealItemResponse> items
) {
}
