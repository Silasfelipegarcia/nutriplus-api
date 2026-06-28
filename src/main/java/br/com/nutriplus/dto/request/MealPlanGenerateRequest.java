package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.PlanRegenerationReason;
import jakarta.validation.constraints.NotNull;

public record MealPlanGenerateRequest(
        @NotNull PlanRegenerationReason reason,
        Long reviewId
) {
}
