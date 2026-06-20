package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.MealPlanGenerationStatus;

public record MealPlanGenerationStatusResponse(
        Long jobId,
        MealPlanGenerationStatus status,
        Long mealPlanId,
        String errorMessage,
        String progressHint
) {
}
