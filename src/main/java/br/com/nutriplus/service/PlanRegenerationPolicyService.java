package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.MealPlanGenerationJob;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.ProgressReview;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.MealPlanGenerationStatus;
import br.com.nutriplus.domain.enums.PlanRegenerationReason;
import br.com.nutriplus.domain.enums.ProgressReviewStatus;
import br.com.nutriplus.dto.response.PlanRegenerationEligibilityResponse;
import br.com.nutriplus.dto.response.ProgressScheduleResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.repository.MealPlanGenerationJobRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.ProgressReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlanRegenerationPolicyService {

    private static final int CYCLE_REVIEW_GRACE_MINUTES = 60;

    private final NutritionProfileRepository nutritionProfileRepository;
    private final MealPlanRepository mealPlanRepository;
    private final MealPlanGenerationJobRepository jobRepository;
    private final ProgressReviewRepository reviewRepository;
    private final ProgressScheduleService progressScheduleService;
    private final FeatureFlagService featureFlagService;
    private final PlanResetService planResetService;

    public PlanRegenerationPolicyService(NutritionProfileRepository nutritionProfileRepository,
                                         MealPlanRepository mealPlanRepository,
                                         MealPlanGenerationJobRepository jobRepository,
                                         ProgressReviewRepository reviewRepository,
                                         ProgressScheduleService progressScheduleService,
                                         FeatureFlagService featureFlagService,
                                         PlanResetService planResetService) {
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.jobRepository = jobRepository;
        this.reviewRepository = reviewRepository;
        this.progressScheduleService = progressScheduleService;
        this.featureFlagService = featureFlagService;
        this.planResetService = planResetService;
    }

    private boolean isUnlimitedRegenEnabled() {
        return featureFlagService.isEnabled("UNLIMITED_PLAN_REGEN");
    }

    public PlanRegenerationEligibilityResponse getEligibility(User user,
                                                            NutritionProfile profile,
                                                            HealthEligibilityService.EligibilityInfo eligibilityInfo) {
        boolean hasMealPlan = !mealPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty();
        ProgressScheduleResponse schedule = progressScheduleService.getScheduleForUser(user.getId(), profile);
        LocalDate today = LocalDate.now();
        LocalDate lockedUntil = profile.getPlanRegenLockedUntil();
        int daysUntilUnlock = lockedUntil == null || !today.isBefore(lockedUntil)
                ? 0
                : (int) ChronoUnit.DAYS.between(today, lockedUntil);

        boolean oneTimeAvailable = profile.getOneTimeCorrectionUsedAt() == null;
        boolean athleteAvailable = profile.isAthleteRegenEligible();
        Long pendingReviewId = findPendingCycleReviewId(user.getId());

        if (isUnlimitedRegenEnabled() && hasMealPlan) {
            oneTimeAvailable = true;
            daysUntilUnlock = 0;
            lockedUntil = null;
        }

        List<String> allowed = new ArrayList<>();
        if (!hasMealPlan) {
            allowed.add(PlanRegenerationReason.FIRST_PLAN.name());
        }
        if (hasFailedGenerationRetry(user.getId())) {
            allowed.add(PlanRegenerationReason.GENERATION_RETRY.name());
        }
        if (athleteAvailable) {
            allowed.add(PlanRegenerationReason.ATHLETE_SWITCH.name());
        }
        if (hasMealPlan && oneTimeAvailable) {
            allowed.add(PlanRegenerationReason.ONE_TIME_CORRECTION.name());
        }
        if (pendingReviewId != null) {
            allowed.add(PlanRegenerationReason.CYCLE_REVIEW.name());
        }
        if (hasMealPlan && isUnlimitedRegenEnabled()) {
            allowed.add(PlanRegenerationReason.UNLOCKED_REGEN.name());
        }
        if (hasMealPlan) {
            allowed.add(PlanRegenerationReason.PLAN_RESET.name());
        }

        PlanResetService.CurrentPlanTrackingSummary trackingSummary =
                planResetService.computeTrackingSummary(user.getId());

        return new PlanRegenerationEligibilityResponse(
                allowed,
                lockedUntil,
                daysUntilUnlock,
                oneTimeAvailable,
                athleteAvailable,
                schedule.due(),
                schedule.due() ? 0 : schedule.daysUntilDue(),
                schedule.nextDueOn(),
                hasMealPlan,
                pendingReviewId,
                eligibilityInfo.aiPlanEligible(),
                eligibilityInfo.aiPlanIneligibleReason(),
                eligibilityInfo.aiPlanIneligibleMessagePt(),
                trackingSummary.planResetAvailable(),
                trackingSummary.currentPlanStarted(),
                trackingSummary.currentPlanCheckinCount(),
                trackingSummary.currentPlanDaysActive()
        );
    }

    public void assertAllowed(User user,
                              NutritionProfile profile,
                              PlanRegenerationReason reason,
                              Long reviewId) {
        switch (reason) {
            case FIRST_PLAN -> {
                if (!mealPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty()) {
                    throw new BusinessException("Você já possui um plano alimentar. Use a reavaliação ou correção única.");
                }
            }
            case GENERATION_RETRY -> {
                if (!hasFailedGenerationRetry(user.getId())) {
                    throw new BusinessException("Não há geração com falha para tentar novamente.");
                }
            }
            case NUTRITIONIST_BYPASS -> {
                // Pro/nutritionist path — quota still applies elsewhere
            }
            case ATHLETE_SWITCH -> {
                requireMealPlan(user.getId());
                if (!profile.isAthleteRegenEligible()) {
                    throw new BusinessException(
                            "A geração por modo atleta não está disponível. Ative o modo atleta ou aguarde a próxima reavaliação.");
                }
            }
            case ONE_TIME_CORRECTION -> {
                requireMealPlan(user.getId());
                if (profile.getOneTimeCorrectionUsedAt() != null) {
                    throw new BusinessException(
                            "Você já usou sua correção única. Aguarde a reavaliação de 15 dias na aba Evolução.");
                }
            }
            case CYCLE_REVIEW -> {
                requireMealPlan(user.getId());
                assertCycleReviewAllowed(user.getId(), reviewId, profile);
            }
            case UNLOCKED_REGEN -> {
                requireMealPlan(user.getId());
                if (!isUnlimitedRegenEnabled()) {
                    throw new BusinessException("Regeneração livre não está habilitada.");
                }
            }
            case PLAN_RESET -> requireMealPlan(user.getId());
            case HOUSEHOLD_SHARED_PLAN -> { }
            default -> throw new BusinessException("Motivo de geração inválido.");
        }

        if (reason != PlanRegenerationReason.FIRST_PLAN
                && reason != PlanRegenerationReason.GENERATION_RETRY
                && reason != PlanRegenerationReason.NUTRITIONIST_BYPASS
                && reason != PlanRegenerationReason.ATHLETE_SWITCH
                && reason != PlanRegenerationReason.ONE_TIME_CORRECTION
                && reason != PlanRegenerationReason.CYCLE_REVIEW
                && reason != PlanRegenerationReason.UNLOCKED_REGEN
                && reason != PlanRegenerationReason.PLAN_RESET
                && reason != PlanRegenerationReason.HOUSEHOLD_SHARED_PLAN) {
            throw new BusinessException("Motivo de geração inválido.");
        }

        if (isPlanRegenLocked(profile)
                && reason != PlanRegenerationReason.ATHLETE_SWITCH
                && reason != PlanRegenerationReason.ONE_TIME_CORRECTION
                && reason != PlanRegenerationReason.CYCLE_REVIEW
                && reason != PlanRegenerationReason.GENERATION_RETRY
                && reason != PlanRegenerationReason.NUTRITIONIST_BYPASS
                && reason != PlanRegenerationReason.UNLOCKED_REGEN
                && reason != PlanRegenerationReason.PLAN_RESET
                && reason != PlanRegenerationReason.HOUSEHOLD_SHARED_PLAN) {
            int days = (int) ChronoUnit.DAYS.between(LocalDate.now(), profile.getPlanRegenLockedUntil());
            throw new BusinessException(
                    days > 0
                            ? "Seu plano segue por mais " + days + " dias. Aguarde a reavaliação na aba Evolução."
                            : "Aguarde a reavaliação na aba Evolução para gerar um novo plano.");
        }
    }

    @Transactional
    public void onGenerationCompleted(User user,
                                      NutritionProfile profile,
                                      MealPlanGenerationJob job) {
        PlanRegenerationReason reason = job.getRegenerationReason();
        if (reason == null) {
            return;
        }

        switch (reason) {
            case ATHLETE_SWITCH -> {
                profile.setAthleteRegenEligible(false);
                profile.setLastAthleteRegenAt(LocalDateTime.now());
            }
            case ONE_TIME_CORRECTION -> profile.setOneTimeCorrectionUsedAt(LocalDateTime.now());
            case CYCLE_REVIEW -> {
                if (job.getProgressReviewId() != null) {
                    reviewRepository.findById(job.getProgressReviewId()).ifPresent(review -> {
                        review.setPlanRegenConsumed(true);
                        reviewRepository.save(review);
                    });
                }
            }
            case UNLOCKED_REGEN -> profile.setPlanRegenLockedUntil(null);
            case FIRST_PLAN, GENERATION_RETRY, NUTRITIONIST_BYPASS, PLAN_RESET, HOUSEHOLD_SHARED_PLAN -> { }
        }

        if (reason != PlanRegenerationReason.UNLOCKED_REGEN) {
            LocalDate lockUntil = LocalDate.now().plusDays(profile.getProgressReviewIntervalDays());
            profile.setPlanRegenLockedUntil(lockUntil);
        }

        nutritionProfileRepository.save(profile);
    }

    @Transactional
    public void markAthleteRegenEligible(NutritionProfile profile) {
        profile.setAthleteRegenEligible(true);
        nutritionProfileRepository.save(profile);
    }

    private void requireMealPlan(Long userId) {
        if (mealPlanRepository.findByUserIdOrderByCreatedAtDesc(userId).isEmpty()) {
            throw new BusinessException("Gere seu primeiro plano alimentar antes de regerar.");
        }
    }

    private boolean isPlanRegenLocked(NutritionProfile profile) {
        LocalDate lockedUntil = profile.getPlanRegenLockedUntil();
        return lockedUntil != null && LocalDate.now().isBefore(lockedUntil);
    }

    private boolean hasFailedGenerationRetry(Long userId) {
        return jobRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(j -> j.getStatus() == MealPlanGenerationStatus.FAILED)
                .orElse(false);
    }

    private Long findPendingCycleReviewId(Long userId) {
        return reviewRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .filter(this::isPendingCycleReview)
                .map(ProgressReview::getId)
                .orElse(null);
    }

    private boolean isPendingCycleReview(ProgressReview review) {
        if (review.getStatus() != ProgressReviewStatus.COMPLETED) {
            return false;
        }
        if (!Boolean.TRUE.equals(review.getPlanChangeSuggested())) {
            return false;
        }
        if (review.isPlanRegenConsumed()) {
            return false;
        }
        if (review.getCompletedAt() == null) {
            return false;
        }
        return review.getCompletedAt().isAfter(LocalDateTime.now().minusMinutes(CYCLE_REVIEW_GRACE_MINUTES));
    }

    private void assertCycleReviewAllowed(Long userId, Long reviewId, NutritionProfile profile) {
        if (reviewId == null) {
            throw new BusinessException("Informe a reavaliação que autoriza a nova geração.");
        }
        ProgressReview review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new BusinessException("Reavaliação não encontrada."));
        if (!isPendingCycleReview(review)) {
            throw new BusinessException(
                    "Esta reavaliação não autoriza nova geração. Conclua uma reavaliação com sugestão de mudança.");
        }
        ProgressScheduleResponse schedule = progressScheduleService.getScheduleForUser(userId, profile);
        if (!schedule.due()) {
            throw new BusinessException(
                    "A reavaliação periódica ainda não está disponível.");
        }
    }
}
