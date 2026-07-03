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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanResetServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;
    @Mock
    private MealRepository mealRepository;
    @Mock
    private DailyMealCheckinRepository checkinRepository;
    @Mock
    private DailyFoodExtraRepository foodExtraRepository;
    @Mock
    private ProgressReviewRepository reviewRepository;
    @Mock
    private BodyMeasurementSessionRepository measurementRepository;
    @Mock
    private NutriCacheEvictionService cacheEvictionService;

    private PlanResetService service;
    private User user;
    private MealPlan plan;

    @BeforeEach
    void setUp() {
        service = new PlanResetService(
                mealPlanRepository,
                mealRepository,
                checkinRepository,
                foodExtraRepository,
                reviewRepository,
                measurementRepository,
                cacheEvictionService
        );
        plan = MealPlan.builder()
                .id(7L)
                .planDate(LocalDate.of(2026, 6, 20))
                .createdAt(LocalDateTime.of(2026, 6, 20, 10, 0))
                .build();
    }

    private User userWithId(long userId) {
        User u = org.mockito.Mockito.mock(User.class);
        when(u.getId()).thenReturn(userId);
        return u;
    }

    @Test
    void computeTrackingSummaryUnavailableWhenNoPlan() {
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(42L)).thenReturn(List.of());

        var summary = service.computeTrackingSummary(42L);

        assertFalse(summary.planResetAvailable());
        assertFalse(summary.currentPlanStarted());
        assertEquals(0, summary.currentPlanCheckinCount());
        assertEquals(0, summary.currentPlanDaysActive());
    }

    @Test
    void computeTrackingSummaryReflectsCheckins() {
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(42L)).thenReturn(List.of(plan));
        when(mealRepository.findByMealPlanIdOrderBySortOrderAsc(7L)).thenReturn(List.of(meal(100L), meal(101L)));
        when(checkinRepository.countByUserIdAndMealIdIn(eq(42L), any())).thenReturn(3L);

        var summary = service.computeTrackingSummary(42L);

        assertTrue(summary.planResetAvailable());
        assertTrue(summary.currentPlanStarted());
        assertEquals(3, summary.currentPlanCheckinCount());
        assertTrue(summary.currentPlanDaysActive() >= 1);
    }

    @Test
    void purgeDeletesTrackingForCurrentPlanEra() {
        User user = userWithId(42L);
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(42L)).thenReturn(List.of(plan));
        when(mealRepository.findByMealPlanIdOrderBySortOrderAsc(7L)).thenReturn(List.of(meal(100L)));

        service.purgeCurrentPlanTracking(user);

        verify(checkinRepository).deleteByUserIdAndMealIdIn(42L, List.of(100L));
        verify(foodExtraRepository).deleteByUserIdAndEntryDateGreaterThanEqual(42L, LocalDate.of(2026, 6, 20));
        verify(reviewRepository).deleteByUserIdAndCreatedAtGreaterThanEqual(
                42L, LocalDate.of(2026, 6, 20).atStartOfDay());
        verify(measurementRepository).deleteByUserIdAndMeasuredOnGreaterThanEqual(
                42L, LocalDate.of(2026, 6, 20));
        verify(cacheEvictionService).evictPlanTrackingCaches(42L);
    }

    private static Meal meal(Long id) {
        Meal meal = Meal.builder().build();
        meal.setId(id);
        return meal;
    }
}
