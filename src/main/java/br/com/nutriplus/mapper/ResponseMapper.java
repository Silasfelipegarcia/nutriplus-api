package br.com.nutriplus.mapper;

import br.com.nutriplus.client.dto.AiShoppingGuidanceDto;
import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.enums.PlanSource;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.dto.response.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class ResponseMapper {

    private final ObjectMapper objectMapper;

    public ResponseMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public UserResponse toUserResponse(br.com.nutriplus.domain.entity.User user, boolean hasNutritionProfile) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                hasNutritionProfile,
                photoForClient(user.getPhotoThumbnailUrl()),
                user.getTermsAcceptedAt(),
                user.getTermsVersion(),
                user.getPrivacyPolicyAcceptedAt()
        );
    }

    public UserResponse toUserResponse(User user, boolean hasNutritionProfile) {
        return new UserResponse(
                user.id(),
                user.name(),
                user.email(),
                user.createdAt(),
                hasNutritionProfile,
                user.photoForClient(),
                user.termsAcceptedAt(),
                user.termsVersion(),
                user.privacyPolicyAcceptedAt()
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
                profile.getGoalTargetWeeks(),
                profile.getGoal(),
                profile.getActivityLevel(),
                profile.getDietaryPreference(),
                profile.getRestriction(),
                profile.getAgentPersona(),
                profile.getFoodLikes(),
                profile.getFoodDislikes(),
                profile.getMealNotes(),
                profile.getCalculationMethod(),
                profile.getBodyFatPercent(),
                profile.getLeanMassKg(),
                profile.getMuscleMassKg(),
                profile.getBmrKcal(),
                profile.getTdeeKcal(),
                profile.getTargetCalories(),
                profile.getTargetProteinG(),
                profile.getTargetCarbsG(),
                profile.getTargetFatG(),
                profile.getUpdatedAt(),
                profile.isAthleteModeEnabled(),
                formatTime(profile.getWakeTime()),
                formatTime(profile.getSleepTime()),
                profile.getHealthConditions(),
                profile.getMedications(),
                profile.getAllergies(),
                profile.getHealthNotes()
        );
    }

    public MealPlanResponse toMealPlanResponse(MealPlan plan, List<Meal> meals, Map<Long, List<MealItem>> itemsByMealId) {
        List<MealResponse> mealResponses = meals.stream()
                .map(meal -> toMealResponse(meal, itemsByMealId.getOrDefault(meal.getId(), List.of())))
                .toList();

        return new MealPlanResponse(
                plan.getId(),
                plan.getPlanDate(),
                plan.getTotalCalories(),
                plan.getTotalProteinG(),
                plan.getTotalCarbsG(),
                plan.getTotalFatG(),
                plan.getDisclaimer(),
                mealResponses,
                plan.getCreatedAt(),
                plan.getMedicalReviewStatus(),
                plan.getMedicalReviewNotes(),
                plan.getPlanSource() != null ? plan.getPlanSource().name() : PlanSource.AI_ONLY.name()
        );
    }

    private static String formatTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private MealResponse toMealResponse(Meal meal, List<MealItem> items) {
        List<MealItemResponse> itemResponses = items.stream()
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
                formatTime(meal.getScheduledTime()),
                itemResponses
        );
    }

    public ShoppingListResponse toShoppingListResponse(ShoppingList list) {
        List<ShoppingListItemResponse> items = list.getItems().stream()
                .map(item -> new ShoppingListItemResponse(
                        item.getId(),
                        item.getItemName(),
                        item.getQuantity(),
                        item.getCategory(),
                        item.getFoodType(),
                        item.getProteinLeanness(),
                        item.getKcalEstimate(),
                        item.getExplanation(),
                        parseAlternatives(item.getAlternativesJson())
                ))
                .toList();

        return new ShoppingListResponse(
                list.getId(),
                list.getWeekStart(),
                list.getWeekEnd(),
                items,
                parseGuidance(list.getGuidanceJson()),
                list.getCreatedAt()
        );
    }

    private List<String> parseAlternatives(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private ShoppingGuidanceResponse parseGuidance(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            AiShoppingGuidanceDto dto = objectMapper.readValue(json, AiShoppingGuidanceDto.class);
            List<SatietyTipResponse> tips = dto.satietyTips() == null ? List.of() : dto.satietyTips().stream()
                    .map(t -> new SatietyTipResponse(t.title(), t.description(), t.category()))
                    .toList();
            List<FlexibleOptionResponse> flex = dto.flexibleOptions() == null ? List.of() : dto.flexibleOptions().stream()
                    .map(o -> new FlexibleOptionResponse(
                            o.id(), o.label(), o.kcalEstimate(), o.impactSummary(),
                            o.deficitPercent(), o.daysDelay(), o.evandroStatus(), o.evandroNote()))
                    .toList();
            return new ShoppingGuidanceResponse(tips, flex, dto.weeklyImpactSummary());
        } catch (Exception e) {
            return null;
        }
    }
}
