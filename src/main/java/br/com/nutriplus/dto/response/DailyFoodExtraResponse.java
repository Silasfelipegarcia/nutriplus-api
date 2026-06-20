package br.com.nutriplus.dto.response;

public record DailyFoodExtraResponse(
        Long id,
        String description,
        int estimatedCalories,
        String impactMessage
) {
}
