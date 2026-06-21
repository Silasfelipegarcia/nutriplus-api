package br.com.nutriplus.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MealPlanResponse(
        Long id,
        LocalDate planDate,
        BigDecimal totalCalories,
        BigDecimal totalProteinG,
        BigDecimal totalCarbsG,
        BigDecimal totalFatG,
        String disclaimer,
        List<MealResponse> meals,
        LocalDateTime createdAt,
        String medicalReviewStatus,
        String medicalReviewNotes,
        String dietReviewStatus,
        String dietReviewNotes,
        String seniorReviewStatus,
        String seniorReviewNotes,
        String planSource
) {
}
