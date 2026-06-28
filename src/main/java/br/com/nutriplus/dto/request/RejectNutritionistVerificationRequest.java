package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Size;

public record RejectNutritionistVerificationRequest(
        @Size(max = 500) String reason
) {
}
