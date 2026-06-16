package br.com.nutriplus.mapper;

import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResponseMapper {

    public UserResponse toUserResponse(User user, boolean hasNutritionProfile) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .hasNutritionProfile(hasNutritionProfile)
                .build();
    }

    public NutritionProfileResponse toNutritionProfileResponse(NutritionProfile profile) {
        return NutritionProfileResponse.builder()
                .id(profile.getId())
                .age(profile.getAge())
                .sex(profile.getSex())
                .heightCm(profile.getHeightCm())
                .currentWeightKg(profile.getCurrentWeightKg())
                .targetWeightKg(profile.getTargetWeightKg())
                .goal(profile.getGoal())
                .activityLevel(profile.getActivityLevel())
                .dietaryPreference(profile.getDietaryPreference())
                .restriction(profile.getRestriction())
                .bmrKcal(profile.getBmrKcal())
                .tdeeKcal(profile.getTdeeKcal())
                .targetCalories(profile.getTargetCalories())
                .targetProteinG(profile.getTargetProteinG())
                .targetCarbsG(profile.getTargetCarbsG())
                .targetFatG(profile.getTargetFatG())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    public MealPlanResponse toMealPlanResponse(MealPlan plan) {
        List<MealResponse> meals = plan.getMeals().stream()
                .map(this::toMealResponse)
                .toList();

        return MealPlanResponse.builder()
                .id(plan.getId())
                .planDate(plan.getPlanDate())
                .totalCalories(plan.getTotalCalories())
                .totalProteinG(plan.getTotalProteinG())
                .totalCarbsG(plan.getTotalCarbsG())
                .totalFatG(plan.getTotalFatG())
                .disclaimer(plan.getDisclaimer())
                .meals(meals)
                .createdAt(plan.getCreatedAt())
                .build();
    }

    private MealResponse toMealResponse(Meal meal) {
        List<MealItemResponse> items = meal.getItems().stream()
                .map(item -> MealItemResponse.builder()
                        .id(item.getId())
                        .foodName(item.getFoodName())
                        .quantityG(item.getQuantityG())
                        .calories(item.getCalories())
                        .proteinG(item.getProteinG())
                        .carbsG(item.getCarbsG())
                        .fatG(item.getFatG())
                        .build())
                .toList();

        return MealResponse.builder()
                .id(meal.getId())
                .mealType(meal.getMealType())
                .name(meal.getName())
                .items(items)
                .build();
    }

    public ShoppingListResponse toShoppingListResponse(ShoppingList list) {
        List<ShoppingListItemResponse> items = list.getItems().stream()
                .map(item -> ShoppingListItemResponse.builder()
                        .id(item.getId())
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .category(item.getCategory())
                        .build())
                .toList();

        return ShoppingListResponse.builder()
                .id(list.getId())
                .weekStart(list.getWeekStart())
                .weekEnd(list.getWeekEnd())
                .items(items)
                .createdAt(list.getCreatedAt())
                .build();
    }
}
