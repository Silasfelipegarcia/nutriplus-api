package br.com.nutriplus.client.dto;

import java.math.BigDecimal;

public record AiNutritionCalculateResponse(
        BigDecimal bmrKcal,
        BigDecimal tdeeKcal,
        BigDecimal targetCalories,
        BigDecimal targetProteinG,
        BigDecimal targetCarbsG,
        BigDecimal targetFatG
) {
}
