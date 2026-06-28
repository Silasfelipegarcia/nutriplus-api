package br.com.nutriplus.mapper;

import br.com.nutriplus.client.dto.AiShoppingGuidanceDto;
import br.com.nutriplus.client.dto.AiShoppingSwapOptionDto;
import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.enums.PlanSource;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.domain.util.LifeStageUtil;
import br.com.nutriplus.dto.response.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nutriplus.service.HealthReferenceService;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class ResponseMapper {

    private final ObjectMapper objectMapper;
    private final HealthReferenceService healthReferenceService;
    private final br.com.nutriplus.infrastructure.security.CpfProtectionService cpfProtectionService;

    public ResponseMapper(ObjectMapper objectMapper,
                          HealthReferenceService healthReferenceService,
                          br.com.nutriplus.infrastructure.security.CpfProtectionService cpfProtectionService) {
        this.objectMapper = objectMapper;
        this.healthReferenceService = healthReferenceService;
        this.cpfProtectionService = cpfProtectionService;
    }
    public UserResponse toUserResponse(br.com.nutriplus.domain.entity.User user, boolean hasNutritionProfile) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                hasNutritionProfile,
                photoForClient(user.getPhotoThumbnailUrl()),
                cpfProtectionService.maskFromEncrypted(user.getCpfEncrypted()),
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
                cpfProtectionService.maskFromEncrypted(user.cpfEncrypted()),
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
        return toNutritionProfileResponse(profile, null);
    }

    public NutritionProfileResponse toNutritionProfileResponse(NutritionProfile profile,
                                                               SubscriptionStatusResponse subscriptionStatus) {
        String lifeStage = profile.getBirthDate() != null
                ? LifeStageUtil.resolveFromBirthDate(profile.getBirthDate()).name()
                : LifeStageUtil.resolve(profile.getAge()).name();

        return new NutritionProfileResponse(
                profile.getId(),
                profile.getAge(),
                profile.getBirthDate(),
                profile.getStateCode(),
                profile.getCity(),
                profile.getChewingDifficulty(),
                profile.isSeniorWeightLossAck(),
                lifeStage,
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
                profile.isEatsBreakfast(),
                profile.isEatsLunch(),
                profile.isEatsAfternoonSnack(),
                profile.isEatsDinner(),
                profile.isOpenToRoutineAdjustment(),
                parseFreeExtras(profile.getFreeExtrasJson()),
                profile.getFoodBudgetLevel(),
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
                profile.getTrainingDailyExtraKcal(),
                profile.getUpdatedAt(),
                profile.isAthleteModeEnabled(),
                formatTime(profile.getWakeTime()),
                formatTime(profile.getSleepTime()),
                profile.getHealthConditions(),
                profile.getMedications(),
                profile.getAllergies(),
                profile.getHealthNotes(),
                healthReferenceService.buildBmiSnapshot(profile),
                subscriptionStatus
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
                plan.getDietReviewStatus(),
                plan.getDietReviewNotes(),
                plan.getSeniorReviewStatus(),
                plan.getSeniorReviewNotes(),
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
                        parseAlternatives(item.getAlternativesJson()),
                        item.getSwapGroup(),
                        parseSwapOptions(item.getSwapOptionsJson()),
                        parseStringList(item.getMarketTipsJson()),
                        item.getDefaultOptionId(),
                        item.getRecommendedOptionId(),
                        item.getSelectedSwapId()
                ))
                .toList();

        boolean pendingSwapReview = list.getItems().stream()
                .anyMatch(item -> hasSwapOptions(item) && (item.getSelectedSwapId() == null || item.getSelectedSwapId().isBlank()));

        return new ShoppingListResponse(
                list.getId(),
                list.getMealPlan() != null ? list.getMealPlan().getId() : null,
                list.getWeekStart(),
                list.getWeekEnd(),
                items,
                parseGuidance(list.getGuidanceJson()),
                pendingSwapReview,
                list.getCreatedAt()
        );
    }

    private List<String> parseAlternatives(String json) {
        return parseStringList(json);
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<ShoppingSwapOptionResponse> parseSwapOptions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<AiShoppingSwapOptionDto> dtos = objectMapper.readValue(json, new TypeReference<List<AiShoppingSwapOptionDto>>() {});
            return dtos.stream()
                    .map(o -> new ShoppingSwapOptionResponse(
                            o.id(),
                            o.label(),
                            o.costTier(),
                            o.whyCheaper(),
                            o.proteinLeanness(),
                            o.kcalEstimate(),
                            o.matchesMealFoods() != null ? o.matchesMealFoods() : List.of()
                    ))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private boolean hasSwapOptions(ShoppingListItem item) {
        return item.getSwapOptionsJson() != null
                && !item.getSwapOptionsJson().isBlank()
                && !item.getSwapOptionsJson().equals("[]");
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
            return new ShoppingGuidanceResponse(
                    tips, flex, dto.weeklyImpactSummary(), dto.budgetSummary(),
                    dto.routineCurrentSummary(), dto.routineSuggestedSummary(), dto.routineGentleNote());
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> parseFreeExtras(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
