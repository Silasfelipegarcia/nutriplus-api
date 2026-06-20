package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.CheckinStatus;
import jakarta.validation.constraints.NotNull;

public record MealCheckinRequest(
        @NotNull Long mealId,
        @NotNull CheckinStatus status,
        String notes
) {
}
