package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Size;

public record ProGenerateMealPlanRequest(
        @Size(max = 4000) String nutritionistNotes
) {
}
