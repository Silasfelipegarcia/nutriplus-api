package br.com.nutriplus.dto.response;

import java.math.BigDecimal;

public record DailyFoodExtraResponse(
        Long id,
        String description,
        int estimatedCalories,
        BigDecimal estimatedCarbsG,
        String impactMessage,
        Long mealId
) {
}
