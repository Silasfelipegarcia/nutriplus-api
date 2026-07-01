package br.com.nutriplus.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DailyAdherenceResponse(
        LocalDate date,
        int mealsCompleted,
        int mealsSkipped,
        int mealsTotal,
        int mealsExpected,
        int mealsMissed,
        int mealsPending,
        int adherencePercent,
        int consumedCalories,
        int extraCalories,
        int totalIntakeCalories,
        Integer targetCalories,
        String dayStatus,
        List<DailyFoodExtraResponse> extras
) {
}
