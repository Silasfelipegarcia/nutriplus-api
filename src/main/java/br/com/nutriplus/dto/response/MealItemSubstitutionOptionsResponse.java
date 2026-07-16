package br.com.nutriplus.dto.response;

import java.util.List;

public record MealItemSubstitutionOptionsResponse(
        Long mealId,
        Long mealItemId,
        MealItemResponse current,
        List<MealItemResponse> substitutions
) {
}
