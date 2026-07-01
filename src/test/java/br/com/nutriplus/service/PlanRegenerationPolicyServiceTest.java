package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.PlanRegenerationReason;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                featureFlagService
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
        when(featureFlagService.isEnabled("UNLIMITED_PLAN_REGEN")).thenReturn(true);
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(org.mockito.Mockito.mock(br.com.nutriplus.domain.entity.MealPlan.class)));
        profile.setOneTimeCorrectionUsedAt(LocalDateTime.now());
        profile.setPlanRegenLockedUntil(java.time.LocalDate.now().plusDays(10));

        assertDoesNotThrow(() -> service.assertAllowed(
                user, profile, PlanRegenerationReason.UNLOCKED_REGEN, null));
    }
}
