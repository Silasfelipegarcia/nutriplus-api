package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitAppFeedbackRequest(
        @NotNull @Min(1) @Max(5) Integer easeOfUse,
        @NotNull @Min(1) @Max(5) Integer mealPlanQuality,
        @NotNull @Min(1) @Max(5) Integer aiHelpfulness,
        @NotNull @Min(1) @Max(5) Integer progressTracking,
        @NotNull @Min(1) @Max(5) Integer overallSatisfaction,
        @Size(max = 4000) String improvementSuggestions,
        @Size(max = 32) String appVersion,
        @Size(max = 16) String platform
) {
}
