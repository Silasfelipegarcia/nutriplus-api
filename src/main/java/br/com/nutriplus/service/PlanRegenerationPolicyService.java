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
    private final ProgressService progressService;

    public PlanRegenerationPolicyService(NutritionProfileRepository nutritionProfileRepository,
                                         MealPlanRepository mealPlanRepository,
                                         MealPlanGenerationJobRepository jobRepository,
                                         ProgressReviewRepository reviewRepository,
                                         ProgressService progressService) {
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.jobRepository = jobRepository;
        this.reviewRepository = reviewRepository;
        this.progressService = progressService;
    }

    public PlanRegenerationEligibilityResponse getEligibility(User user, NutritionProfile profile) {
        boolean hasMealPlan = !mealPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty();
        ProgressScheduleResponse schedule = progressService.getScheduleForUser(user.getId(), profile);
        LocalDate today = LocalDate.now();
        LocalDate lockedUntil = profile.getPlanRegenLockedUntil();
        int daysUntilUnlock = lockedUntil == null || !today.isBefore(lockedUntil)
                ? 0
                : (int) ChronoUnit.DAYS.between(today, lockedUntil);

        boolean oneTimeAvailable = profile.getOneTimeCorrectionUsedAt() == null;
        boolean athleteAvailable = profile.isAthleteRegenEligible();
        Long pendingReviewId = findPendingCycleReviewId(user.getId());

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
                pendingReviewId
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
                assertCycleReviewAllowed(user.getId(), reviewId);
            }
            default -> throw new BusinessException("Motivo de geração inválido.");
        }

        if (reason != PlanRegenerationReason.FIRST_PLAN
                && reason != PlanRegenerationReason.GENERATION_RETRY
                && reason != PlanRegenerationReason.NUTRITIONIST_BYPASS
                && reason != PlanRegenerationReason.ATHLETE_SWITCH
                && reason != PlanRegenerationReason.ONE_TIME_CORRECTION
                && reason != PlanRegenerationReason.CYCLE_REVIEW) {
            throw new BusinessException("Motivo de geração inválido.");
        }

        if (isPlanRegenLocked(profile)
                && reason != PlanRegenerationReason.ATHLETE_SWITCH
                && reason != PlanRegenerationReason.ONE_TIME_CORRECTION
                && reason != PlanRegenerationReason.CYCLE_REVIEW
                && reason != PlanRegenerationReason.GENERATION_RETRY
                && reason != PlanRegenerationReason.NUTRITIONIST_BYPASS) {
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

        LocalDate lockUntil = LocalDate.now().plusDays(profile.getProgressReviewIntervalDays());
        profile.setPlanRegenLockedUntil(lockUntil);

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
            case FIRST_PLAN, GENERATION_RETRY, NUTRITIONIST_BYPASS -> { }
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

    private void assertCycleReviewAllowed(Long userId, Long reviewId) {
        if (reviewId == null) {
            throw new BusinessException("Informe a reavaliação que autoriza a nova geração.");
        }
        ProgressReview review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new BusinessException("Reavaliação não encontrada."));
        if (!isPendingCycleReview(review)) {
            throw new BusinessException(
                    "Esta reavaliação não autoriza nova geração. Conclua uma reavaliação com sugestão de mudança.");
        }
        ProgressScheduleResponse schedule = progressService.getSchedule();
        if (!schedule.due()) {
            throw new BusinessException(
                    "A reavaliação periódica ainda não está disponível.");
        }
    }
}
