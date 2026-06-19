package br.com.nutriplus.mapper;

import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResponseMapper {

    public UserResponse toUserResponse(br.com.nutriplus.domain.entity.User user, boolean hasNutritionProfile) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                hasNutritionProfile,
                photoForClient(user.getPhotoThumbnailUrl())
        );
    }

    public UserResponse toUserResponse(User user, boolean hasNutritionProfile) {
        return new UserResponse(
                user.id(),
                user.name(),
                user.email(),
                user.createdAt(),
                hasNutritionProfile,
                user.photoForClient()
        );
    }

    private static String photoForClient(String photoThumbnailUrl) {
        if (photoThumbnailUrl != null && !photoThumbnailUrl.isBlank()) {
            return photoThumbnailUrl;
        }
        return null;
    }

    public NutritionProfileResponse toNutritionProfileResponse(NutritionProfile profile) {
        return new NutritionProfileResponse(
                profile.getId(),
                profile.getAge(),
                profile.getSex(),
                profile.getHeightCm(),
                profile.getCurrentWeightKg(),
                profile.getTargetWeightKg(),
                profile.getGoal(),
                profile.getActivityLevel(),
                profile.getDietaryPreference(),
                profile.getRestriction(),
                profile.getAgentPersona(),
                profile.getFoodLikes(),
                profile.getFoodDislikes(),
                profile.getMealNotes(),
                profile.getBmrKcal(),
                profile.getTdeeKcal(),
                profile.getTargetCalories(),
                profile.getTargetProteinG(),
                profile.getTargetCarbsG(),
                profile.getTargetFatG(),
                profile.getUpdatedAt()
        );
    }

    public MealPlanResponse toMealPlanResponse(MealPlan plan) {
        List<MealResponse> meals = plan.getMeals().stream()
                .map(this::toMealResponse)
                .toList();

        return new MealPlanResponse(
                plan.getId(),
                plan.getPlanDate(),
                plan.getTotalCalories(),
                plan.getTotalProteinG(),
                plan.getTotalCarbsG(),
                plan.getTotalFatG(),
                plan.getDisclaimer(),
                meals,
                plan.getCreatedAt()
        );
    }

    private MealResponse toMealResponse(Meal meal) {
        List<MealItemResponse> items = meal.getItems().stream()
                .map(item -> new MealItemResponse(
                        item.getId(),
                        item.getFoodName(),
                        item.getQuantityG(),
                        item.getCalories(),
                        item.getProteinG(),
                        item.getCarbsG(),
                        item.getFatG()
                ))
                .toList();

        return new MealResponse(
                meal.getId(),
                meal.getMealType(),
                meal.getName(),
                items
        );
    }

    public ShoppingListResponse toShoppingListResponse(ShoppingList list) {
        List<ShoppingListItemResponse> items = list.getItems().stream()
                .map(item -> new ShoppingListItemResponse(
                        item.getId(),
                        item.getItemName(),
                        item.getQuantity(),
                        item.getCategory()
                ))
                .toList();

        return new ShoppingListResponse(
                list.getId(),
                list.getWeekStart(),
                list.getWeekEnd(),
                items,
                list.getCreatedAt()
        );
    }
}
