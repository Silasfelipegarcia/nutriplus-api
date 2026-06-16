package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.*;
import br.com.nutriplus.config.AppProperties;
import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.enums.AiRequestStatus;
import br.com.nutriplus.domain.enums.AiRequestType;
import br.com.nutriplus.domain.enums.MealType;
import br.com.nutriplus.dto.response.MealPlanResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.ShoppingListRepository;
import br.com.nutriplus.security.CurrentUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final CurrentUser currentUser;
    private final NutritionProfileService nutritionProfileService;
    private final MealPlanRepository mealPlanRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final AiAgentClient aiAgentClient;
    private final AiRequestLogService aiRequestLogService;
    private final AppProperties appProperties;
    private final ResponseMapper responseMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public MealPlanResponse generate() {
        User user = currentUser.get();
        NutritionProfile profile = nutritionProfileService.getEntityForUser(user);
        long start = System.currentTimeMillis();
        String requestJson = "{}";

        try {
            requestJson = objectMapper.writeValueAsString(profile.getId());
            AiMealPlanGenerateResponse aiResponse = aiAgentClient.generateMealPlan(profile);

            MealPlan mealPlan = MealPlan.builder()
                    .user(user)
                    .nutritionProfile(profile)
                    .planDate(LocalDate.now())
                    .totalCalories(aiResponse.getTotalCalories())
                    .totalProteinG(aiResponse.getTotalProteinG())
                    .totalCarbsG(aiResponse.getTotalCarbsG())
                    .totalFatG(aiResponse.getTotalFatG())
                    .disclaimer(appProperties.getDisclaimer())
                    .aiModel(aiResponse.getAiModel())
                    .meals(new ArrayList<>())
                    .build();

            if (aiResponse.getMeals() != null) {
                for (AiMealDto aiMeal : aiResponse.getMeals()) {
                    Meal meal = Meal.builder()
                            .mealPlan(mealPlan)
                            .mealType(MealType.valueOf(aiMeal.getMealType()))
                            .name(aiMeal.getName())
                            .sortOrder(aiMeal.getSortOrder() != null ? aiMeal.getSortOrder() : 0)
                            .items(new ArrayList<>())
                            .build();

                    if (aiMeal.getItems() != null) {
                        for (AiMealItemDto aiItem : aiMeal.getItems()) {
                            MealItem item = MealItem.builder()
                                    .meal(meal)
                                    .foodName(aiItem.getFoodName())
                                    .quantityG(aiItem.getQuantityG())
                                    .calories(aiItem.getCalories())
                                    .proteinG(aiItem.getProteinG())
                                    .carbsG(aiItem.getCarbsG())
                                    .fatG(aiItem.getFatG())
                                    .build();
                            meal.getItems().add(item);
                        }
                    }
                    mealPlan.getMeals().add(meal);
                }
            }

            mealPlan = mealPlanRepository.save(mealPlan);
            saveShoppingList(user, mealPlan, aiResponse.getShoppingList());

            aiRequestLogService.log(user, AiRequestType.GENERATE_MEAL_PLAN, requestJson,
                    objectMapper.writeValueAsString(aiResponse), AiRequestStatus.SUCCESS, null,
                    (int) (System.currentTimeMillis() - start));

            return responseMapper.toMealPlanResponse(mealPlan);
        } catch (Exception e) {
            aiRequestLogService.log(user, AiRequestType.GENERATE_MEAL_PLAN, requestJson,
                    null, AiRequestStatus.ERROR, e.getMessage(),
                    (int) (System.currentTimeMillis() - start));
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e);
        }
    }

    public MealPlanResponse getLatest() {
        User user = currentUser.get();
        List<MealPlan> plans = mealPlanRepository.findByUserIdWithMealsOrderByCreatedAtDesc(user.getId());
        if (plans.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum plano alimentar encontrado");
        }
        return responseMapper.toMealPlanResponse(plans.getFirst());
    }

    private void saveShoppingList(User user, MealPlan mealPlan, List<AiShoppingItemDto> aiItems) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        ShoppingList shoppingList = ShoppingList.builder()
                .user(user)
                .mealPlan(mealPlan)
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .items(new ArrayList<>())
                .build();

        if (aiItems != null) {
            for (AiShoppingItemDto aiItem : aiItems) {
                ShoppingListItem item = ShoppingListItem.builder()
                        .shoppingList(shoppingList)
                        .itemName(aiItem.getItemName())
                        .quantity(aiItem.getQuantity())
                        .category(aiItem.getCategory())
                        .build();
                shoppingList.getItems().add(item);
            }
        }

        shoppingListRepository.save(shoppingList);
    }
}
