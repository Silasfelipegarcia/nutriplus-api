package br.com.nutriplus.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record TodayCheckinsResponse(
        List<TodayMealCheckinResponse> meals,
        int completedCount,
        int totalCount,
        Integer targetCalories,
        int consumedCalories,
        int extraCalories,
        int totalIntakeCalories,
        int remainingCalories,
        String goal,
        List<DailyFoodExtraResponse> extras,
        BigDecimal targetCarbsG,
        BigDecimal consumedCarbsG,
        BigDecimal extraCarbsG,
        BigDecimal remainingCarbsG,
        String nutritionMode
) {
    public TodayCheckinsResponse(
            List<TodayMealCheckinResponse> meals,
            int completedCount,
            int totalCount,
            Integer targetCalories,
            int consumedCalories,
            int extraCalories,
            int totalIntakeCalories,
            int remainingCalories,
            String goal,
            List<DailyFoodExtraResponse> extras
    ) {
        this(meals, completedCount, totalCount, targetCalories, consumedCalories, extraCalories,
                totalIntakeCalories, remainingCalories, goal, extras,
                null, null, null, null, "STANDARD");
    }
}
