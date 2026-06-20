package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Size;

public record PublishMealPlanRequest(
        @Size(max = 4000) String reviewNotes
) {
}
