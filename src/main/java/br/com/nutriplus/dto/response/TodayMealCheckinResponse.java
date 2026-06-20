package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.CheckinStatus;
import br.com.nutriplus.domain.enums.MealType;

public record TodayMealCheckinResponse(
        Long mealId,
        MealType mealType,
        String mealName,
        CheckinStatus status,
        Integer mealCalories
) {
}
