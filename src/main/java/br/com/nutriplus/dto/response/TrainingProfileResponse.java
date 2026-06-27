package br.com.nutriplus.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record TrainingProfileResponse(
        boolean athleteModeEnabled,
        List<TrainingActivityResponse> activities,
        BigDecimal weeklyTrainingKcal,
        BigDecimal dailyExtraKcal,
        BigDecimal baseTargetCalories,
        BigDecimal adjustedTargetCalories,
        boolean appliedToPlan,
        boolean weightRequired,
        CoachInsightResponse coachInsight
) {
}
