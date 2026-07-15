package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.ProgressReview;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.PlanRegenerationReason;
import br.com.nutriplus.domain.enums.ProgressReviewStatus;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.repository.MealPlanGenerationJobRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.ProgressReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanRegenerationPolicyServiceTest {

    @Mock
    private NutritionProfileRepository nutritionProfileRepository;
    @Mock
    private MealPlanRepository mealPlanRepository;
    @Mock
    private MealPlanGenerationJobRepository jobRepository;
    @Mock
    private ProgressReviewRepository reviewRepository;
    @Mock
    private ProgressScheduleService progressScheduleService;
    @Mock
    private FeatureFlagService featureFlagService;
    @Mock
    private PlanResetService planResetService;

    private PlanRegenerationPolicyService service;
    private User user;
    private NutritionProfile profile;

    @BeforeEach
    void setUp() {
        service = new PlanRegenerationPolicyService(
                nutritionProfileRepository,
                mealPlanRepository,
                jobRepository,
                reviewRepository,
                progressScheduleService,
                featureFlagService,
                planResetService
        );
        user = org.mockito.Mockito.mock(User.class);
        org.mockito.Mockito.when(user.getId()).thenReturn(1L);
        profile = NutritionProfile.builder()
                .user(user)
                .progressReviewIntervalDays(15)
                .build();
    }

    @Test
    void firstPlanAllowedWhenNoExistingPlan() {
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> service.assertAllowed(
                user, profile, PlanRegenerationReason.FIRST_PLAN, null));
    }

    @Test
    void oneTimeCorrectionBlockedAfterUsed() {
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(org.mockito.Mockito.mock(br.com.nutriplus.domain.entity.MealPlan.class)));
        profile.setOneTimeCorrectionUsedAt(LocalDateTime.now());

        assertThrows(BusinessException.class, () -> service.assertAllowed(
                user, profile, PlanRegenerationReason.ONE_TIME_CORRECTION, null));
    }

    @Test
    void unlockedRegenAllowedWhenFeatureFlagOn() {
        when(featureFlagService.isUnlimitedPlanRegenEnabled()).thenReturn(true);
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(org.mockito.Mockito.mock(br.com.nutriplus.domain.entity.MealPlan.class)));
        profile.setOneTimeCorrectionUsedAt(LocalDateTime.now());
        profile.setPlanRegenLockedUntil(java.time.LocalDate.now().plusDays(10));

        assertDoesNotThrow(() -> service.assertAllowed(
                user, profile, PlanRegenerationReason.UNLOCKED_REGEN, null));
    }

    @Test
    void planResetBypassesRegenLock() {
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(org.mockito.Mockito.mock(br.com.nutriplus.domain.entity.MealPlan.class)));
        profile.setPlanRegenLockedUntil(java.time.LocalDate.now().plusDays(10));

        assertDoesNotThrow(() -> service.assertAllowed(
                user, profile, PlanRegenerationReason.PLAN_RESET, null));
    }

    @Test
    void planResetRequiresExistingPlan() {
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());

        assertThrows(BusinessException.class, () -> service.assertAllowed(
                user, profile, PlanRegenerationReason.PLAN_RESET, null));
    }

    @Test
    void cycleReviewAllowedAfterScheduleNoLongerDue() {
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(mock(br.com.nutriplus.domain.entity.MealPlan.class)));
        ProgressReview review = mock(ProgressReview.class);
        when(review.getStatus()).thenReturn(ProgressReviewStatus.COMPLETED);
        when(review.getPlanChangeSuggested()).thenReturn(true);
        when(review.isPlanRegenConsumed()).thenReturn(false);
        when(review.getCompletedAt()).thenReturn(LocalDateTime.now().minusMinutes(5));
        when(reviewRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(review));

        assertDoesNotThrow(() -> service.assertAllowed(
                user, profile, PlanRegenerationReason.CYCLE_REVIEW, 7L));
    }

    @Test
    void cycleReviewBlockedWhenGraceWindowExpired() {
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(mock(br.com.nutriplus.domain.entity.MealPlan.class)));
        ProgressReview review = mock(ProgressReview.class);
        when(review.getStatus()).thenReturn(ProgressReviewStatus.COMPLETED);
        when(review.getPlanChangeSuggested()).thenReturn(true);
        when(review.isPlanRegenConsumed()).thenReturn(false);
        when(review.getCompletedAt()).thenReturn(LocalDateTime.now().minusMinutes(61));
        when(reviewRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(review));

        assertThrows(BusinessException.class, () -> service.assertAllowed(
                user, profile, PlanRegenerationReason.CYCLE_REVIEW, 7L));
    }
}
