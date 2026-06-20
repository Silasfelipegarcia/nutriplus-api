package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiMealDto;
import br.com.nutriplus.client.dto.AiMealItemDto;
import br.com.nutriplus.client.dto.AiMealPlanGenerateResponse;
import br.com.nutriplus.client.dto.AiShoppingGuidanceDto;
import br.com.nutriplus.client.dto.AiShoppingItemDto;
import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.enums.AiRequestStatus;
import br.com.nutriplus.domain.enums.AiRequestType;
import br.com.nutriplus.domain.enums.MealPlanGenerationStatus;
import br.com.nutriplus.domain.enums.MealType;
import br.com.nutriplus.infrastructure.config.AppProperties;
import br.com.nutriplus.repository.MealItemRepository;
import br.com.nutriplus.repository.MealPlanGenerationJobRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.MealRepository;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.ShoppingListRepository;
import br.com.nutriplus.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class MealPlanGenerationProcessor {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final MealPlanGenerationJobRepository jobRepository;
    private final UserRepository userRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final MealPlanRepository mealPlanRepository;
    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final AiAgentClient aiAgentClient;
    private final AiRequestLogService aiRequestLogService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    public MealPlanGenerationProcessor(MealPlanGenerationJobRepository jobRepository,
                                       UserRepository userRepository,
                                       NutritionProfileRepository nutritionProfileRepository,
                                       MealPlanRepository mealPlanRepository,
                                       MealRepository mealRepository,
                                       MealItemRepository mealItemRepository,
                                       ShoppingListRepository shoppingListRepository,
                                       AiAgentClient aiAgentClient,
                                       AiRequestLogService aiRequestLogService,
                                       AppProperties appProperties,
                                       ObjectMapper objectMapper,
                                       AuditLogService auditLogService) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.mealRepository = mealRepository;
        this.mealItemRepository = mealItemRepository;
        this.shoppingListRepository = shoppingListRepository;
        this.aiAgentClient = aiAgentClient;
        this.aiRequestLogService = aiRequestLogService;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public void run(Long jobId, Long userId, long startMs) throws Exception {
        MealPlanGenerationJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Job não encontrado"));

        if (job.getStatus() != MealPlanGenerationStatus.PENDING) {
            return;
        }

        job.setStatus(MealPlanGenerationStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        jobRepository.save(job);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));
        NutritionProfile profile = nutritionProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Perfil nutricional não encontrado"));

        String requestJson = objectMapper.writeValueAsString(profile.getId());
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
                .medicalReviewStatus(aiResponse.medicalReviewStatus())
                .medicalReviewNotes(aiResponse.medicalReviewNotes())
                .build();

        mealPlan = mealPlanRepository.save(mealPlan);

        if (aiResponse.meals() != null) {
            for (AiMealDto aiMeal : aiResponse.meals()) {
                LocalTime scheduledTime = parseScheduledTime(aiMeal.scheduledTime());
                int sortOrder = aiMeal.sortOrder() != null ? aiMeal.sortOrder() : sortOrderFromTime(scheduledTime);

                Meal meal = Meal.builder()
                        .mealPlan(mealPlan)
                        .mealType(MealType.valueOf(aiMeal.mealType()))
                        .name(aiMeal.name())
                        .sortOrder(sortOrder)
                        .scheduledTime(scheduledTime)
                        .build();

                meal = mealRepository.save(meal);

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
                        mealItemRepository.save(item);
                    }
                }
            }
        }

        saveShoppingList(user, mealPlan, aiResponse.shoppingList(), aiResponse.shoppingGuidance());

        job.setStatus(MealPlanGenerationStatus.COMPLETED);
        job.setMealPlan(mealPlan);
        job.setCompletedAt(LocalDateTime.now());
        jobRepository.save(job);

        aiRequestLogService.log(user, AiRequestType.GENERATE_MEAL_PLAN, requestJson,
                objectMapper.writeValueAsString(aiResponse), AiRequestStatus.SUCCESS, null,
                (int) (System.currentTimeMillis() - startMs));

        auditLogService.log("MEAL_PLAN_GENERATED", "MEAL_PLAN", String.valueOf(mealPlan.getId()), user, null);
    }

    @Transactional
    public void markFailed(Long jobId, String message) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(MealPlanGenerationStatus.FAILED);
            job.setErrorMessage(message);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        });
    }

    private LocalTime parseScheduledTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalTime.parse(value.trim(), TIME_FMT);
    }

    private int sortOrderFromTime(LocalTime time) {
        if (time == null) {
            return 0;
        }
        return time.getHour() * 60 + time.getMinute();
    }

    private void saveShoppingList(User user, MealPlan mealPlan, List<AiShoppingItemDto> aiItems,
                                AiShoppingGuidanceDto guidance) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        String guidanceJson = null;
        try {
            if (guidance != null) {
                guidanceJson = objectMapper.writeValueAsString(guidance);
            }
        } catch (Exception ignored) {
            guidanceJson = null;
        }

        ShoppingList shoppingList = ShoppingList.builder()
                .user(user)
                .mealPlan(mealPlan)
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .guidanceJson(guidanceJson)
                .items(new ArrayList<>())
                .build();

        if (aiItems != null) {
            for (AiShoppingItemDto aiItem : aiItems) {
                String alternativesJson = null;
                try {
                    if (aiItem.alternatives() != null && !aiItem.alternatives().isEmpty()) {
                        alternativesJson = objectMapper.writeValueAsString(aiItem.alternatives());
                    }
                } catch (Exception ignored) {
                    alternativesJson = null;
                }
                ShoppingListItem item = ShoppingListItem.builder()
                        .shoppingList(shoppingList)
                        .itemName(aiItem.itemName())
                        .quantity(aiItem.quantity())
                        .category(aiItem.category())
                        .foodType(aiItem.foodType())
                        .proteinLeanness(aiItem.proteinLeanness())
                        .kcalEstimate(aiItem.kcalEstimate())
                        .explanation(aiItem.explanation())
                        .alternativesJson(alternativesJson)
                        .build();
                shoppingList.getItems().add(item);
            }
        }

        shoppingListRepository.save(shoppingList);
    }
}
