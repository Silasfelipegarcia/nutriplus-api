package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.Meal;
import br.com.nutriplus.domain.entity.MealPlan;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.infrastructure.config.NutriCacheEvictionService;
import br.com.nutriplus.repository.BodyMeasurementSessionRepository;
import br.com.nutriplus.repository.DailyFoodExtraRepository;
import br.com.nutriplus.repository.DailyMealCheckinRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.MealRepository;
import br.com.nutriplus.repository.ProgressReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PlanResetService {

    public record CurrentPlanTrackingSummary(
            boolean planResetAvailable,
            boolean currentPlanStarted,
            int currentPlanCheckinCount,
            int currentPlanDaysActive
    ) {
        public static CurrentPlanTrackingSummary unavailable() {
            return new CurrentPlanTrackingSummary(false, false, 0, 0);
        }
    }

    private final MealPlanRepository mealPlanRepository;
    private final MealRepository mealRepository;
    private final DailyMealCheckinRepository checkinRepository;
    private final DailyFoodExtraRepository foodExtraRepository;
    private final ProgressReviewRepository reviewRepository;
    private final BodyMeasurementSessionRepository measurementRepository;
    private final NutriCacheEvictionService cacheEvictionService;

    public PlanResetService(MealPlanRepository mealPlanRepository,
                            MealRepository mealRepository,
                            DailyMealCheckinRepository checkinRepository,
                            DailyFoodExtraRepository foodExtraRepository,
                            ProgressReviewRepository reviewRepository,
                            BodyMeasurementSessionRepository measurementRepository,
                            NutriCacheEvictionService cacheEvictionService) {
        this.mealPlanRepository = mealPlanRepository;
        this.mealRepository = mealRepository;
        this.checkinRepository = checkinRepository;
        this.foodExtraRepository = foodExtraRepository;
        this.reviewRepository = reviewRepository;
        this.measurementRepository = measurementRepository;
        this.cacheEvictionService = cacheEvictionService;
    }

    public CurrentPlanTrackingSummary computeTrackingSummary(Long userId) {
        return resolveLatestPlan(userId)
                .map(plan -> {
                    LocalDate planStart = planStartDate(plan);
                    List<Long> mealIds = mealIdsForPlan(plan.getId());
                    long checkinCount = mealIds.isEmpty()
                            ? 0L
                            : checkinRepository.countByUserIdAndMealIdIn(userId, mealIds);
                    int daysActive = (int) ChronoUnit.DAYS.between(planStart, LocalDate.now()) + 1;
                    if (daysActive < 1) {
                        daysActive = 1;
                    }
                    return new CurrentPlanTrackingSummary(
                            true,
                            checkinCount > 0,
                            (int) checkinCount,
                            daysActive);
                })
                .orElse(CurrentPlanTrackingSummary.unavailable());
    }

    @Transactional
    public void purgeCurrentPlanTracking(User user) {
        Long userId = user.getId();
        MealPlan plan = resolveLatestPlan(userId)
                .orElseThrow(() -> new IllegalStateException("Nenhum plano ativo para zerar."));
        LocalDate planStart = planStartDate(plan);
        LocalDateTime planStartDateTime = planStart.atStartOfDay();

        List<Long> mealIds = mealIdsForPlan(plan.getId());
        if (!mealIds.isEmpty()) {
            checkinRepository.deleteByUserIdAndMealIdIn(userId, mealIds);
        }
        foodExtraRepository.deleteByUserIdAndEntryDateGreaterThanEqual(userId, planStart);
        reviewRepository.deleteByUserIdAndCreatedAtGreaterThanEqual(userId, planStartDateTime);
        measurementRepository.deleteByUserIdAndMeasuredOnGreaterThanEqual(userId, planStart);

        cacheEvictionService.evictPlanTrackingCaches(userId);
    }

    private java.util.Optional<MealPlan> resolveLatestPlan(Long userId) {
        List<MealPlan> plans = mealPlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (plans.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(plans.getFirst());
    }

    private static LocalDate planStartDate(MealPlan plan) {
        if (plan.getPlanDate() != null) {
            return plan.getPlanDate();
        }
        if (plan.getCreatedAt() != null) {
            return plan.getCreatedAt().toLocalDate();
        }
        return LocalDate.now();
    }

    private List<Long> mealIdsForPlan(Long mealPlanId) {
        return mealRepository.findByMealPlanIdOrderBySortOrderAsc(mealPlanId).stream()
                .map(Meal::getId)
                .toList();
    }
}
