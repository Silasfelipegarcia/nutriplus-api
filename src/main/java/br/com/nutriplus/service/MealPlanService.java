package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.*;
import br.com.nutriplus.infrastructure.config.AppProperties;
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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
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
    private final AuditLogService auditLogService;
    private final MeterRegistry meterRegistry;

    public MealPlanService(CurrentUser currentUser,
                           NutritionProfileService nutritionProfileService,
                           MealPlanRepository mealPlanRepository,
                           ShoppingListRepository shoppingListRepository,
                           AiAgentClient aiAgentClient,
                           AiRequestLogService aiRequestLogService,
                           AppProperties appProperties,
                           ResponseMapper responseMapper,
                           ObjectMapper objectMapper,
                           AuditLogService auditLogService,
                           MeterRegistry meterRegistry) {
        this.currentUser = currentUser;
        this.nutritionProfileService = nutritionProfileService;
        this.mealPlanRepository = mealPlanRepository;
        this.shoppingListRepository = shoppingListRepository;
        this.aiAgentClient = aiAgentClient;
        this.aiRequestLogService = aiRequestLogService;
        this.appProperties = appProperties;
        this.responseMapper = responseMapper;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public MealPlanResponse generate() {
        Timer.Sample sample = Timer.start(meterRegistry);
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
                    .totalCalories(aiResponse.totalCalories())
                    .totalProteinG(aiResponse.totalProteinG())
                    .totalCarbsG(aiResponse.totalCarbsG())
                    .totalFatG(aiResponse.totalFatG())
                    .disclaimer(appProperties.disclaimer())
                    .aiModel(aiResponse.aiModel())
                    .meals(new ArrayList<>())
                    .build();

            if (aiResponse.meals() != null) {
                for (AiMealDto aiMeal : aiResponse.meals()) {
                    Meal meal = Meal.builder()
                            .mealPlan(mealPlan)
                            .mealType(MealType.valueOf(aiMeal.mealType()))
                            .name(aiMeal.name())
                            .sortOrder(aiMeal.sortOrder() != null ? aiMeal.sortOrder() : 0)
                            .items(new ArrayList<>())
                            .build();

                    if (aiMeal.items() != null) {
                        for (AiMealItemDto aiItem : aiMeal.items()) {
                            MealItem item = MealItem.builder()
                                    .meal(meal)
                                    .foodName(aiItem.foodName())
                                    .quantityG(aiItem.quantityG())
                                    .calories(aiItem.calories())
                                    .proteinG(aiItem.proteinG())
                                    .carbsG(aiItem.carbsG())
                                    .fatG(aiItem.fatG())
                                    .build();
                            meal.getItems().add(item);
                        }
                    }
                    mealPlan.getMeals().add(meal);
                }
            }

            mealPlan = mealPlanRepository.save(mealPlan);
            saveShoppingList(user, mealPlan, aiResponse.shoppingList());

            aiRequestLogService.log(user, AiRequestType.GENERATE_MEAL_PLAN, requestJson,
                    objectMapper.writeValueAsString(aiResponse), AiRequestStatus.SUCCESS, null,
                    (int) (System.currentTimeMillis() - start));

            auditLogService.log("MEAL_PLAN_GENERATED", "MEAL_PLAN", String.valueOf(mealPlan.getId()), user, null);

            return responseMapper.toMealPlanResponse(mealPlan);
        } catch (Exception e) {
            aiRequestLogService.log(user, AiRequestType.GENERATE_MEAL_PLAN, requestJson,
                    null, AiRequestStatus.ERROR, e.getMessage(),
                    (int) (System.currentTimeMillis() - start));
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e);
        } finally {
            sample.stop(Timer.builder("nutriplus.meal_plan.generation.duration")
                    .register(meterRegistry));
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
                        .itemName(aiItem.itemName())
                        .quantity(aiItem.quantity())
                        .category(aiItem.category())
                        .build();
                shoppingList.getItems().add(item);
            }
        }

        shoppingListRepository.save(shoppingList);
    }
}
