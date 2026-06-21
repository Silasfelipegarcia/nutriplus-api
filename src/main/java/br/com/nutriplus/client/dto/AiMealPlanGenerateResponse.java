package br.com.nutriplus.client.dto;

import java.math.BigDecimal;
import java.util.List;

public record AiMealPlanGenerateResponse(
        String aiModel,
        BigDecimal totalCalories,
        BigDecimal totalProteinG,
        BigDecimal totalCarbsG,
        BigDecimal totalFatG,
        List<AiMealDto> meals,
        List<AiShoppingItemDto> shoppingList,
        AiShoppingGuidanceDto shoppingGuidance,
        String medicalReviewStatus,
        String medicalReviewNotes,
        String dietReviewStatus,
        String dietReviewNotes,
        String seniorReviewStatus,
        String seniorReviewNotes
) {
}
