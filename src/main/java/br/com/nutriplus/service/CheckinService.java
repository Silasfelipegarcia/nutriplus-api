package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiFoodExtraEstimateResponse;
import br.com.nutriplus.domain.entity.DailyFoodExtra;
import br.com.nutriplus.domain.entity.DailyMealCheckin;
import br.com.nutriplus.domain.entity.Meal;
import br.com.nutriplus.domain.entity.MealItem;
import br.com.nutriplus.domain.entity.MealPlan;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.CheckinStatus;
import br.com.nutriplus.domain.enums.Goal;
import br.com.nutriplus.dto.request.FoodExtraRequest;
import br.com.nutriplus.dto.request.MealCheckinRequest;
import br.com.nutriplus.dto.response.CheckinStatsResponse;
import br.com.nutriplus.dto.response.DailyFoodExtraResponse;
import br.com.nutriplus.dto.response.TodayCheckinsResponse;
import br.com.nutriplus.dto.response.TodayMealCheckinResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.DailyFoodExtraRepository;
import br.com.nutriplus.repository.DailyMealCheckinRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.MealRepository;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CheckinService {

    private final CurrentUser currentUser;
    private final MealPlanRepository mealPlanRepository;
    private final MealRepository mealRepository;
    private final DailyMealCheckinRepository checkinRepository;
    private final DailyFoodExtraRepository foodExtraRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final MealLoader mealLoader;
    private final AiAgentClient aiAgentClient;

    public CheckinService(CurrentUser currentUser,
                          MealPlanRepository mealPlanRepository,
                          MealRepository mealRepository,
                          DailyMealCheckinRepository checkinRepository,
                          DailyFoodExtraRepository foodExtraRepository,
                          NutritionProfileRepository nutritionProfileRepository,
                          MealLoader mealLoader,
                          AiAgentClient aiAgentClient) {
        this.currentUser = currentUser;
        this.mealPlanRepository = mealPlanRepository;
        this.mealRepository = mealRepository;
        this.checkinRepository = checkinRepository;
        this.foodExtraRepository = foodExtraRepository;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.mealLoader = mealLoader;
        this.aiAgentClient = aiAgentClient;
    }

    public TodayCheckinsResponse getToday() {
        User user = currentUser.get();
        LocalDate today = LocalDate.now();
        NutritionProfile profile = nutritionProfileRepository.findByUserId(user.getId()).orElse(null);
        Integer target = targetCalories(profile);
        String goal = profile != null ? profile.getGoal().name() : Goal.MAINTAIN_WEIGHT.name();

        List<DailyFoodExtra> extras = foodExtraRepository.findByUserIdAndEntryDateOrderByCreatedAtAsc(user.getId(), today);
        int extraCalories = extras.stream().mapToInt(DailyFoodExtra::getEstimatedCalories).sum();
        List<DailyFoodExtraResponse> extraResponses = extras.stream().map(this::toExtraResponse).toList();

        List<MealPlan> plans = mealPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (plans.isEmpty()) {
            int remaining = target != null ? target - extraCalories : 0;
            return new TodayCheckinsResponse(
                    List.of(), 0, 0, target, 0, extraCalories, extraCalories, remaining, goal, extraResponses);
        }

        MealPlan plan = plans.getFirst();
        List<Meal> meals = mealLoader.mealsForPlan(plan.getId());
        Map<Long, List<MealItem>> itemsByMeal = mealLoader.itemsByMealId(meals);

        Map<Long, CheckinStatus> statusByMeal = checkinRepository.findByUserIdAndCheckinDate(user.getId(), today)
                .stream()
                .filter(c -> c.getMeal() != null)
                .collect(Collectors.toMap(c -> c.getMeal().getId(), DailyMealCheckin::getStatus, (a, b) -> b));

        List<TodayMealCheckinResponse> items = meals.stream()
                .map(meal -> {
                    int mealKcal = mealCalories(itemsByMeal.getOrDefault(meal.getId(), List.of()));
                    return new TodayMealCheckinResponse(
                            meal.getId(),
                            meal.getMealType(),
                            meal.getName(),
                            statusByMeal.get(meal.getId()),
                            mealKcal > 0 ? mealKcal : null
                    );
                })
                .toList();

        int consumed = items.stream()
                .filter(i -> i.status() == CheckinStatus.DONE)
                .mapToInt(i -> i.mealCalories() != null ? i.mealCalories() : 0)
                .sum();
        int totalIntake = consumed + extraCalories;
        int remaining = target != null ? target - totalIntake : 0;
        int completed = (int) items.stream().filter(i -> i.status() == CheckinStatus.DONE).count();

        return new TodayCheckinsResponse(
                items, completed, items.size(), target, consumed, extraCalories,
                totalIntake, remaining, goal, extraResponses);
    }

    @Transactional
    public TodayMealCheckinResponse saveCheckin(MealCheckinRequest request) {
        User user = currentUser.get();
        LocalDate today = LocalDate.now();
        Meal meal = mealRepository.findById(request.mealId())
                .orElseThrow(() -> new ResourceNotFoundException("Refeição não encontrada"));

        if (!meal.getMealPlan().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Refeição não encontrada");
        }

        DailyMealCheckin checkin = checkinRepository
                .findByUserIdAndCheckinDateAndMealId(user.getId(), today, meal.getId())
                .orElseGet(() -> DailyMealCheckin.builder()
                        .user(user)
                        .checkinDate(today)
                        .meal(meal)
                        .mealType(meal.getMealType())
                        .status(request.status())
                        .notes(request.notes())
                        .build());

        checkin.setStatus(request.status());
        checkin.setNotes(request.notes());
        checkinRepository.save(checkin);

        int mealKcal = mealCalories(mealLoader.itemsForMeal(meal.getId()));
        return new TodayMealCheckinResponse(
                meal.getId(),
                meal.getMealType(),
                meal.getName(),
                checkin.getStatus(),
                mealKcal > 0 ? mealKcal : null
        );
    }

    @Transactional
    public DailyFoodExtraResponse addFoodExtra(FoodExtraRequest request) {
        User user = currentUser.get();
        LocalDate today = LocalDate.now();
        NutritionProfile profile = nutritionProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil nutricional não encontrado"));

        TodayCheckinsResponse snapshot = getToday();
        AiFoodExtraEstimateResponse estimate = aiAgentClient.estimateFoodExtra(
                profile,
                request.description(),
                snapshot.consumedCalories(),
                snapshot.extraCalories(),
                snapshot.targetCalories());

        DailyFoodExtra saved = foodExtraRepository.save(new DailyFoodExtra(
                user,
                today,
                request.description().trim(),
                estimate.estimatedCalories(),
                estimate.impactMessage()));

        return toExtraResponse(saved);
    }

    public CheckinStatsResponse getStats() {
        User user = currentUser.get();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        List<DailyMealCheckin> weekCheckins = checkinRepository.findByUserIdAndDateRange(user.getId(), weekStart, today);
        int done = (int) weekCheckins.stream().filter(c -> c.getStatus() == CheckinStatus.DONE).count();
        int total = Math.max(weekCheckins.size(), 1);
        int adherence = (int) Math.round((done * 100.0) / total);

        return new CheckinStatsResponse(calculateStreak(user.getId(), today), adherence);
    }

    private DailyFoodExtraResponse toExtraResponse(DailyFoodExtra extra) {
        return new DailyFoodExtraResponse(
                extra.getId(),
                extra.getDescription(),
                extra.getEstimatedCalories(),
                extra.getImpactMessage());
    }

    private Integer targetCalories(NutritionProfile profile) {
        if (profile == null || profile.getTargetCalories() == null) {
            return null;
        }
        return profile.getTargetCalories().intValue();
    }

    private int mealCalories(List<MealItem> items) {
        return items.stream()
                .map(MealItem::getCalories)
                .filter(Objects::nonNull)
                .mapToInt(BigDecimal::intValue)
                .sum();
    }

    private int calculateStreak(Long userId, LocalDate today) {
        int streak = 0;
        LocalDate cursor = today;
        while (true) {
            List<DailyMealCheckin> day = checkinRepository.findByUserIdAndCheckinDate(userId, cursor);
            if (day.isEmpty() || day.stream().noneMatch(c -> c.getStatus() == CheckinStatus.DONE)) {
                break;
            }
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
