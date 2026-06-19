package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NutritionProfileResponse(
        Long id,
        Integer age,
        Sex sex,
        BigDecimal heightCm,
        BigDecimal currentWeightKg,
        BigDecimal targetWeightKg,
        Goal goal,
        ActivityLevel activityLevel,
        DietaryPreference dietaryPreference,
        Restriction restriction,
        AgentPersona agentPersona,
        String foodLikes,
        String foodDislikes,
        String mealNotes,
        BigDecimal bmrKcal,
        BigDecimal tdeeKcal,
        BigDecimal targetCalories,
        BigDecimal targetProteinG,
        BigDecimal targetCarbsG,
        BigDecimal targetFatG,
        LocalDateTime updatedAt
) {
}
