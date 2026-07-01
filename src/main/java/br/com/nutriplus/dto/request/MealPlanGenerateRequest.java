package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.PlanRegenerationReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MealPlanGenerateRequest(
        @NotNull PlanRegenerationReason reason,
        Long reviewId,
        @Size(max = 4000) String nutritionistNotes
) {
}
