package br.com.nutriplus.dto.response;

public record AppBootstrapResponse(
        UserResponse user,
        NutritionProfileResponse nutritionProfile,
        MealPlanResponse mealPlan,
        ShoppingListResponse shoppingList,
        TodayCheckinsResponse checkinsToday,
        CheckinStatsResponse checkinsStats,
        ProgressScheduleResponse progressSchedule,
        MealPlanGenerationStatusResponse generationStatus
) {
}
