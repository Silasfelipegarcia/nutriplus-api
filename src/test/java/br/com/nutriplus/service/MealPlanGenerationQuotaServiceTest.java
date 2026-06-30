package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.SubscriptionRequiredException;
import br.com.nutriplus.infrastructure.config.MealPlanGenerationQuotaProperties;
import br.com.nutriplus.repository.MealPlanGenerationJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealPlanGenerationQuotaServiceTest {

    @Mock
    private BillingEnforcementService billingEnforcementService;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private MealPlanGenerationJobRepository jobRepository;
    @Mock
    private FeatureFlagService featureFlagService;

    private MealPlanGenerationQuotaService quotaService;
    private User user;

    @BeforeEach
    void setUp() {
        quotaService = new MealPlanGenerationQuotaService(
                billingEnforcementService,
                subscriptionService,
                jobRepository,
                new MealPlanGenerationQuotaProperties(1, 1, 3, 2),
                featureFlagService);
        user = User.builder().id(42L).email("user@test.com").build();
        lenient().when(subscriptionService.emTrial(user)).thenReturn(false);
        lenient().when(subscriptionService.temAssinaturaPaga(user)).thenReturn(false);
        lenient().when(subscriptionService.resolverPlanoEfetivo(user)).thenReturn(SubscriptionPlan.FREE);
        lenient().when(featureFlagService.isEnabled("UNLIMITED_PLAN_REGEN")).thenReturn(false);
    }

    @Test
    void betaModeAllowsUntilDailyLimit() {
        when(billingEnforcementService.isBillingEnabled()).thenReturn(false);
        when(jobRepository.countByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
                eq(42L), any(), eq(LocalDate.now().atStartOfDay()))).thenReturn(1L);

        assertDoesNotThrow(() -> quotaService.assertCanGenerate(user));
    }

    @Test
    void unlimitedFlagBypassesDailyAndMonthlyQuota() {
        when(featureFlagService.isEnabled("UNLIMITED_PLAN_REGEN")).thenReturn(true);

        assertDoesNotThrow(() -> quotaService.assertCanGenerate(user));
    }

    @Test
    void betaModeBlocksAfterDailyLimit() {
        when(featureFlagService.isEnabled("UNLIMITED_PLAN_REGEN")).thenReturn(false);
        when(billingEnforcementService.isBillingEnabled()).thenReturn(false);
        when(jobRepository.countByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
                eq(42L), any(), eq(LocalDate.now().atStartOfDay()))).thenReturn(2L);

        assertThrows(BusinessException.class, () -> quotaService.assertCanGenerate(user));
    }

    @Test
    void freeTierBlocksAfterDailyGeneration() {
        when(billingEnforcementService.isBillingEnabled()).thenReturn(true);
        when(jobRepository.countByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
                eq(42L), any(), eq(LocalDate.now().atStartOfDay()))).thenReturn(1L);

        assertThrows(BusinessException.class, () -> quotaService.assertCanGenerate(user));
    }

    @Test
    void freeTierBlocksAfterMonthlyGenerationEvenOnNewDay() {
        when(billingEnforcementService.isBillingEnabled()).thenReturn(true);
        when(jobRepository.countByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
                eq(42L), any(), eq(LocalDate.now().atStartOfDay()))).thenReturn(0L);
        when(jobRepository.countByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
                eq(42L), any(), eq(LocalDate.now().withDayOfMonth(1).atStartOfDay()))).thenReturn(1L);

        assertThrows(SubscriptionRequiredException.class, () -> quotaService.assertCanGenerate(user));
    }

    @Test
    void athleteAllowsUpToThreePerDay() {
        when(billingEnforcementService.isBillingEnabled()).thenReturn(true);
        when(subscriptionService.temAssinaturaPaga(user)).thenReturn(true);
        when(subscriptionService.resolverPlanoEfetivo(user)).thenReturn(SubscriptionPlan.ATHLETE_MONTHLY);
        when(jobRepository.countByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
                eq(42L), any(), eq(LocalDate.now().atStartOfDay()))).thenReturn(2L);

        assertDoesNotThrow(() -> quotaService.assertCanGenerate(user));
    }

    @Test
    void athleteBlocksAfterThirdDailyGeneration() {
        when(billingEnforcementService.isBillingEnabled()).thenReturn(true);
        when(subscriptionService.temAssinaturaPaga(user)).thenReturn(true);
        when(subscriptionService.resolverPlanoEfetivo(user)).thenReturn(SubscriptionPlan.ATHLETE_MONTHLY);
        when(jobRepository.countByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
                eq(42L), any(), eq(LocalDate.now().atStartOfDay()))).thenReturn(3L);

        assertThrows(BusinessException.class, () -> quotaService.assertCanGenerate(user));
    }

    @Test
    void trialUsesUnlimitedTierDailyQuota() {
        when(billingEnforcementService.isBillingEnabled()).thenReturn(true);
        when(subscriptionService.emTrial(user)).thenReturn(true);
        when(jobRepository.countByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
                eq(42L), any(), eq(LocalDate.now().atStartOfDay()))).thenReturn(2L);

        assertDoesNotThrow(() -> quotaService.assertCanGenerate(user));
        assertEquals(1, quotaService.remainingGenerationsToday(user));
    }

    @Test
    void remainingGenerationsTodayForLimitedTier() {
        when(billingEnforcementService.isBillingEnabled()).thenReturn(true);
        when(jobRepository.countByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
                eq(42L), any(), eq(LocalDate.now().atStartOfDay()))).thenReturn(1L);

        assertEquals(0, quotaService.remainingGenerationsToday(user));
    }
}
