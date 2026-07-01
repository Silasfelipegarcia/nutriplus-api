package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Size;

public record PublishMealPlanRequest(
        @Size(max = 4000) String reviewNotes,
        @Size(max = 2000) String changesSummary
) {
}
