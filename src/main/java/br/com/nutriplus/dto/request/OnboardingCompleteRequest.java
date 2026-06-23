package br.com.nutriplus.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OnboardingCompleteRequest(
        @NotNull @Valid NutritionProfileRequest nutritionProfile,
        @NotNull Boolean athleteModeEnabled,
        @Valid List<TrainingActivityRequest> activities
) {
}
