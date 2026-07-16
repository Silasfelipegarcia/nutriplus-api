package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FoodExtraRequest(
        @NotBlank @Size(max = 500) String description,
        Long mealId
) {
}
