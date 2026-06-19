package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record NutritionProfileRequest(
        @NotNull @Min(10) @Max(120) Integer age,
        @NotNull Sex sex,
        @NotNull @DecimalMin("100.0") @DecimalMax("250.0") BigDecimal heightCm,
        @NotNull @DecimalMin("30.0") @DecimalMax("300.0") BigDecimal currentWeightKg,
        @NotNull @DecimalMin("30.0") @DecimalMax("300.0") BigDecimal targetWeightKg,
        @NotNull Goal goal,
        @NotNull ActivityLevel activityLevel,
        @NotNull DietaryPreference dietaryPreference,
        @NotNull Restriction restriction,
        @NotNull AgentPersona agentPersona,
        @Size(max = 2000) String foodLikes,
        @Size(max = 2000) String foodDislikes,
        @Size(max = 2000) String mealNotes
) {
}
