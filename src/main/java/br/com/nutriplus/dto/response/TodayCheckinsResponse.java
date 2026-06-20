package br.com.nutriplus.dto.response;

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
        List<DailyFoodExtraResponse> extras
) {
}
