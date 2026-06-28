package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.CheckinStatus;
import br.com.nutriplus.domain.enums.MealType;

import java.util.List;

public record TodayMealCheckinResponse(
        Long mealId,
        MealType mealType,
        String mealName,
        CheckinStatus status,
        Integer mealCalories,
        List<MealItemResponse> items
) {
    public TodayMealCheckinResponse {
        items = items != null ? items : List.of();
    }
}
