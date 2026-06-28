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
import br.com.nutriplus.dto.response.CheckinAdherenceHistoryResponse;
import br.com.nutriplus.dto.response.CheckinAdherenceProjectionResponse;
import br.com.nutriplus.dto.response.CheckinStatsResponse;
import br.com.nutriplus.dto.response.DailyAdherenceResponse;
import br.com.nutriplus.dto.response.CoachInsightResponse;
import br.com.nutriplus.dto.response.DailyFoodExtraResponse;
import br.com.nutriplus.dto.response.MealItemResponse;
import br.com.nutriplus.dto.response.TodayCheckinsResponse;
import br.com.nutriplus.dto.response.TodayMealCheckinResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.DailyFoodExtraRepository;
import br.com.nutriplus.repository.DailyMealCheckinRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.MealRepository;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import br.com.nutriplus.infrastructure.config.NutriCacheNames;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final CoachInsightService coachInsightService;
    private final TrainingService trainingService;

    public CheckinService(CurrentUser currentUser,
                          MealPlanRepository mealPlanRepository,
                          MealRepository mealRepository,
                          DailyMealCheckinRepository checkinRepository,
                          DailyFoodExtraRepository foodExtraRepository,
                          NutritionProfileRepository nutritionProfileRepository,
                          MealLoader mealLoader,
                          AiAgentClient aiAgentClient,
                          CoachInsightService coachInsightService,
                          @Lazy TrainingService trainingService) {
        this.currentUser = currentUser;
        this.mealPlanRepository = mealPlanRepository;
        this.mealRepository = mealRepository;
        this.checkinRepository = checkinRepository;
        this.foodExtraRepository = foodExtraRepository;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.mealLoader = mealLoader;
        this.aiAgentClient = aiAgentClient;
        this.coachInsightService = coachInsightService;
        this.trainingService = trainingService;
    }

    @Cacheable(value = NutriCacheNames.CHECKINS_TODAY, keyGenerator = "userDateCacheKeyGenerator")
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
                    List<MealItem> mealItems = itemsByMeal.getOrDefault(meal.getId(), List.of());
                    int mealKcal = mealCalories(mealItems);
                    return new TodayMealCheckinResponse(
                            meal.getId(),
                            meal.getMealType(),
                            meal.getName(),
                            statusByMeal.get(meal.getId()),
                            mealKcal > 0 ? mealKcal : null,
                            toItemResponses(mealItems)
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
    @CacheEvict(value = {NutriCacheNames.CHECKINS_TODAY, NutriCacheNames.CHECKINS_STATS, NutriCacheNames.CHECKINS_ADHERENCE}, allEntries = true)
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

        List<MealItem> mealItems = mealLoader.itemsForMeal(meal.getId());
        int mealKcal = mealCalories(mealItems);
        return new TodayMealCheckinResponse(
                meal.getId(),
                meal.getMealType(),
                meal.getName(),
                checkin.getStatus(),
                mealKcal > 0 ? mealKcal : null,
                toItemResponses(mealItems)
        );
    }

    @Transactional
    @CacheEvict(value = {NutriCacheNames.CHECKINS_TODAY, NutriCacheNames.CHECKINS_ADHERENCE}, allEntries = true)
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

    public CoachInsightResponse getBalanceCoachInsight() {
        User user = currentUser.get();
        NutritionProfile profile = nutritionProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil nutricional não encontrado"));
        TodayCheckinsResponse today = getToday();
        var trainingProfile = trainingService.getProfile();
        return coachInsightService.balanceInsight(profile, trainingProfile, today);
    }

    @Cacheable(value = NutriCacheNames.CHECKINS_STATS, keyGenerator = "userDateCacheKeyGenerator")
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

    @Cacheable(value = NutriCacheNames.CHECKINS_ADHERENCE, keyGenerator = "userDaysCacheKeyGenerator")
    public CheckinAdherenceHistoryResponse getAdherenceHistory(int days) {
        int windowDays = normalizeWindowDays(days);
        User user = currentUser.get();
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(windowDays - 1L);

        NutritionProfile profile = nutritionProfileRepository.findByUserId(user.getId()).orElse(null);
        Integer target = targetCalories(profile);
        Goal goal = profile != null ? profile.getGoal() : Goal.MAINTAIN_WEIGHT;

        List<DailyMealCheckin> checkins = checkinRepository.findByUserIdAndDateRange(user.getId(), start, today);
        List<DailyFoodExtra> extras = foodExtraRepository
                .findByUserIdAndEntryDateBetweenOrderByEntryDateAscCreatedAtAsc(user.getId(), start, today);

        Map<LocalDate, List<DailyMealCheckin>> checkinsByDate = checkins.stream()
                .collect(Collectors.groupingBy(DailyMealCheckin::getCheckinDate));
        Map<LocalDate, List<DailyFoodExtra>> extrasByDate = extras.stream()
                .collect(Collectors.groupingBy(DailyFoodExtra::getEntryDate));

        Map<Long, Integer> mealCaloriesCache = new HashMap<>();
        List<DailyAdherenceResponse> daily = new ArrayList<>();
        int totalDone = 0;
        int totalRecords = 0;

        for (int i = 0; i < windowDays; i++) {
            LocalDate date = start.plusDays(i);
            List<DailyMealCheckin> dayCheckins = checkinsByDate.getOrDefault(date, List.of());
            List<DailyFoodExtra> dayExtras = extrasByDate.getOrDefault(date, List.of());

            int completed = (int) dayCheckins.stream().filter(c -> c.getStatus() == CheckinStatus.DONE).count();
            int skipped = (int) dayCheckins.stream().filter(c -> c.getStatus() == CheckinStatus.SKIPPED).count();
            int mealsTotal = dayCheckins.size();
            int consumed = dayCheckins.stream()
                    .filter(c -> c.getStatus() == CheckinStatus.DONE)
                    .mapToInt(c -> mealCaloriesForCheckin(c, mealCaloriesCache))
                    .sum();
            int extraCalories = dayExtras.stream().mapToInt(DailyFoodExtra::getEstimatedCalories).sum();
            int totalIntake = consumed + extraCalories;
            int adherence = mealsTotal == 0 ? 0 : (int) Math.round((completed * 100.0) / mealsTotal);
            String dayStatus = resolveDayStatus(mealsTotal, dayExtras.size(), adherence, totalIntake, target);

            if (mealsTotal > 0) {
                totalDone += completed;
                totalRecords += mealsTotal;
            }

            daily.add(new DailyAdherenceResponse(
                    date,
                    completed,
                    skipped,
                    mealsTotal,
                    adherence,
                    consumed,
                    extraCalories,
                    totalIntake,
                    target,
                    dayStatus,
                    dayExtras.stream().map(this::toExtraResponse).toList()));
        }

        int overallAdherence = totalRecords == 0 ? 0 : (int) Math.round((totalDone * 100.0) / totalRecords);
        CheckinAdherenceProjectionResponse projection = buildProjection(daily, target, goal);

        return new CheckinAdherenceHistoryResponse(
                windowDays,
                overallAdherence,
                calculateStreak(user.getId(), today),
                target,
                goal.name(),
                daily,
                projection);
    }

    private static int normalizeWindowDays(int days) {
        if (days == 14 || days == 30) {
            return days;
        }
        return 7;
    }

    private int mealCaloriesForCheckin(DailyMealCheckin checkin, Map<Long, Integer> cache) {
        if (checkin.getMeal() == null) {
            return 0;
        }
        Long mealId = checkin.getMeal().getId();
        return cache.computeIfAbsent(mealId, id -> mealCalories(mealLoader.itemsForMeal(id)));
    }

    static String resolveDayStatus(int mealsTotal, int extrasCount, int adherencePercent,
                                   int totalIntake, Integer target) {
        if (mealsTotal == 0 && extrasCount == 0) {
            return "NO_DATA";
        }
        if (target != null && target > 0 && totalIntake > target * 1.10) {
            return "OVER";
        }
        if (target != null && target > 0) {
            double ratio = totalIntake / (double) target;
            if (adherencePercent >= 70 && ratio >= 0.85 && ratio <= 1.10) {
                return "ON_TRACK";
            }
            if (adherencePercent < 100 || ratio < 0.50) {
                return "PARTIAL";
            }
            if (adherencePercent >= 70) {
                return "ON_TRACK";
            }
            return "PARTIAL";
        }
        return adherencePercent >= 70 ? "ON_TRACK" : "PARTIAL";
    }

    private CheckinAdherenceProjectionResponse buildProjection(List<DailyAdherenceResponse> daily,
                                                               Integer target,
                                                               Goal goal) {
        List<DailyAdherenceResponse> withData = daily.stream()
                .filter(d -> !"NO_DATA".equals(d.dayStatus()))
                .toList();
        if (withData.isEmpty() || target == null || target <= 0) {
            return new CheckinAdherenceProjectionResponse(
                    0,
                    null,
                    "Registre refeições nos próximos dias para ver uma projeção do seu ritmo.");
        }

        double avgDelta = withData.stream()
                .mapToInt(d -> d.totalIntakeCalories() - target)
                .average()
                .orElse(0);
        int averageDailyCalorieDelta = (int) Math.round(avgDelta);
        double weeklyKg = BigDecimal.valueOf((avgDelta * 7) / 7700.0)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        String summary = projectionSummary(goal, averageDailyCalorieDelta, weeklyKg, withData.size());
        Double estimatedWeight = goal == Goal.MAINTAIN_WEIGHT ? null : weeklyKg;
        return new CheckinAdherenceProjectionResponse(averageDailyCalorieDelta, estimatedWeight, summary);
    }

    private static String projectionSummary(Goal goal, int avgDelta, double weeklyKg, int daysWithData) {
        String window = daysWithData == 1 ? "no último dia com registro" : "nos últimos " + daysWithData + " dias com registro";
        int absDelta = Math.abs(avgDelta);
        if (goal == Goal.LOSE_WEIGHT) {
            if (avgDelta <= -100) {
                return String.format(
                        "No ritmo %s, você fica ~%d kcal/dia abaixo da meta — alinhado com perder ~%.2f kg/semana (estimativa).",
                        window, absDelta, Math.abs(weeklyKg));
            }
            if (avgDelta >= 100) {
                return String.format(
                        "No ritmo %s, você está ~%d kcal/dia acima da meta — a perda de peso fica mais lenta neste período.",
                        window, absDelta);
            }
            return String.format(
                    "No ritmo %s, suas calorias estão próximas da meta — bom equilíbrio para manter o plano.",
                    window);
        }
        if (goal == Goal.GAIN_MASS) {
            if (avgDelta >= 100) {
                return String.format(
                        "No ritmo %s, você fica ~%d kcal/dia acima da meta — favorável para ganho (~%.2f kg/semana, estimativa).",
                        window, absDelta, Math.abs(weeklyKg));
            }
            return String.format(
                    "No ritmo %s, intake ~%d kcal vs meta — ajuste leve se quiser acelerar o ganho.",
                    window, avgDelta);
        }
        return String.format(
                "No ritmo %s, você fica em média %s%d kcal/dia em relação à meta.",
                window, avgDelta >= 0 ? "+" : "", avgDelta);
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

    private List<MealItemResponse> toItemResponses(List<MealItem> items) {
        return items.stream()
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
