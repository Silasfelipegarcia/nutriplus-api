package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.BodyMeasurementSession;
import br.com.nutriplus.domain.entity.MealPlan;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.Goal;
import br.com.nutriplus.dto.response.CheckinAdherenceHistoryResponse;
import br.com.nutriplus.dto.response.CheckinAdherenceProjectionResponse;
import br.com.nutriplus.dto.response.GoalTimelineResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.BodyMeasurementSessionRepository;
import br.com.nutriplus.repository.MealPlanRepository;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class GoalTimelineService {

    private static final double KCAL_PER_KG_FAT = 7700.0;
    private static final double MAX_SAFE_LOSS_KG_PER_WEEK = 1.0;

    private final CurrentUser currentUser;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final MealPlanRepository mealPlanRepository;
    private final BodyMeasurementSessionRepository measurementRepository;
    private final CheckinService checkinService;

    public GoalTimelineService(CurrentUser currentUser,
                               NutritionProfileRepository nutritionProfileRepository,
                               MealPlanRepository mealPlanRepository,
                               BodyMeasurementSessionRepository measurementRepository,
                               @Lazy CheckinService checkinService) {
        this.currentUser = currentUser;
        this.nutritionProfileRepository = nutritionProfileRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.measurementRepository = measurementRepository;
        this.checkinService = checkinService;
    }

    public GoalTimelineResponse getGoalTimeline() {
        User user = currentUser.get();
        NutritionProfile profile = nutritionProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil nutricional não encontrado"));

        if (profile.getGoal() == Goal.MAINTAIN_WEIGHT) {
            return maintainResponse(profile);
        }

        Integer weeks = profile.getGoalTargetWeeks();
        if (weeks == null || weeks <= 0) {
            return insufficientData(profile, "Defina um prazo em semanas no seu perfil para ver a previsão da meta.");
        }

        LocalDate journeyStart = resolveJourneyStart(user.getId(), profile);
        LocalDate targetDate = journeyStart.plusWeeks(weeks);
        LocalDate today = LocalDate.now();

        BigDecimal startWeight = resolveStartWeight(user.getId(), profile);
        BigDecimal targetWeight = profile.getTargetWeightKg();
        BigDecimal latestWeight = resolveLatestWeight(user.getId(), profile);
        BigDecimal deltaTotal = startWeight.subtract(targetWeight).abs();

        double requiredRate = deltaTotal.doubleValue() / weeks;
        if (profile.getGoal() == Goal.LOSE_WEIGHT) {
            requiredRate = Math.min(requiredRate, MAX_SAFE_LOSS_KG_PER_WEEK);
        }
        BigDecimal requiredRateBd = scale(requiredRate);

        BigDecimal actualRate = resolveActualRate(user.getId(), profile, journeyStart, latestWeight, startWeight);
        BigDecimal remainingKg = latestWeight.subtract(targetWeight).abs();

        LocalDate projectedFinish = null;
        String paceStatus = "INSUFFICIENT_DATA";
        int daysAheadOrBehind = 0;
        String summary;

        if (actualRate != null && actualRate.compareTo(BigDecimal.ZERO) > 0 && remainingKg.compareTo(BigDecimal.ZERO) > 0.05) {
            double weeksToGo = remainingKg.doubleValue() / actualRate.doubleValue();
            long daysToGo = Math.round(weeksToGo * 7);
            projectedFinish = today.plusDays(daysToGo);
            daysAheadOrBehind = (int) ChronoUnit.DAYS.between(targetDate, projectedFinish);
            paceStatus = resolvePaceStatus(daysAheadOrBehind);
            summary = buildSummary(profile.getGoal(), targetDate, projectedFinish, daysAheadOrBehind, actualRate, requiredRateBd);
        } else {
            CheckinAdherenceHistoryResponse adherence = checkinService.getAdherenceHistory(14);
            CheckinAdherenceProjectionResponse projection = adherence.projection();
            if (projection.estimatedWeightChangeKgPerWeek() != null
                    && projection.estimatedWeightChangeKgPerWeek() != 0
                    && remainingKg.compareTo(BigDecimal.ZERO) > 0.05) {
                actualRate = scale(Math.abs(projection.estimatedWeightChangeKgPerWeek()));
                double weeksToGo = remainingKg.doubleValue() / actualRate.doubleValue();
                projectedFinish = today.plusDays(Math.round(weeksToGo * 7));
                daysAheadOrBehind = (int) ChronoUnit.DAYS.between(targetDate, projectedFinish);
                paceStatus = resolvePaceStatus(daysAheadOrBehind);
                summary = buildSummary(profile.getGoal(), targetDate, projectedFinish, daysAheadOrBehind, actualRate, requiredRateBd)
                        + " (estimativa pelas calorias registradas)";
            } else {
                summary = "Registre refeições e peso para estimar se você chega na meta em "
                        + weeks + " semanas (até " + formatDate(targetDate) + ").";
            }
        }

        return new GoalTimelineResponse(
                journeyStart,
                targetDate,
                startWeight,
                targetWeight,
                latestWeight,
                requiredRateBd,
                actualRate,
                projectedFinish,
                paceStatus,
                daysAheadOrBehind,
                summary
        );
    }

    private GoalTimelineResponse maintainResponse(NutritionProfile profile) {
        return new GoalTimelineResponse(
                null,
                null,
                profile.getCurrentWeightKg(),
                profile.getTargetWeightKg(),
                profile.getCurrentWeightKg(),
                BigDecimal.ZERO,
                null,
                null,
                "MAINTAIN",
                0,
                "Seu objetivo é manutenção — acompanhe aderência ao plano e medidas corporais."
        );
    }

    private GoalTimelineResponse insufficientData(NutritionProfile profile, String message) {
        return new GoalTimelineResponse(
                null,
                null,
                profile.getCurrentWeightKg(),
                profile.getTargetWeightKg(),
                profile.getCurrentWeightKg(),
                null,
                null,
                null,
                "INSUFFICIENT_DATA",
                0,
                message
        );
    }

    private LocalDate resolveJourneyStart(Long userId, NutritionProfile profile) {
        return mealPlanRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .findFirst()
                .map(MealPlan::getPlanDate)
                .orElse(profile.getUpdatedAt() != null
                        ? profile.getUpdatedAt().toLocalDate()
                        : LocalDate.now());
    }

    private BigDecimal resolveStartWeight(Long userId, NutritionProfile profile) {
        return measurementRepository.findFirstByUserIdOrderByMeasuredOnAscIdAsc(userId)
                .map(BodyMeasurementSession::getWeightKg)
                .orElse(profile.getCurrentWeightKg());
    }

    private BigDecimal resolveLatestWeight(Long userId, NutritionProfile profile) {
        return measurementRepository.findFirstByUserIdOrderByMeasuredOnDescIdDesc(userId)
                .map(BodyMeasurementSession::getWeightKg)
                .orElse(profile.getCurrentWeightKg());
    }

    private BigDecimal resolveActualRate(Long userId,
                                         NutritionProfile profile,
                                         LocalDate journeyStart,
                                         BigDecimal latestWeight,
                                         BigDecimal startWeight) {
        List<BodyMeasurementSession> sessions = measurementRepository
                .findTop12ByUserIdOrderByMeasuredOnAscIdAsc(userId);
        if (sessions.size() >= 2) {
            BodyMeasurementSession first = sessions.getFirst();
            BodyMeasurementSession last = sessions.getLast();
            long days = Math.max(ChronoUnit.DAYS.between(first.getMeasuredOn(), last.getMeasuredOn()), 1);
            BigDecimal weightDelta = last.getWeightKg().subtract(first.getWeightKg());
            if (profile.getGoal() == Goal.LOSE_WEIGHT && weightDelta.compareTo(BigDecimal.ZERO) < 0) {
                double weeks = days / 7.0;
                return scale(weightDelta.abs().doubleValue() / weeks);
            }
            if (profile.getGoal() == Goal.GAIN_MASS && weightDelta.compareTo(BigDecimal.ZERO) > 0) {
                double weeks = days / 7.0;
                return scale(weightDelta.doubleValue() / weeks);
            }
        }
        BigDecimal moved = latestWeight.subtract(startWeight);
        long daysSinceStart = Math.max(ChronoUnit.DAYS.between(journeyStart, LocalDate.now()), 1);
        double weeks = daysSinceStart / 7.0;
        if (profile.getGoal() == Goal.LOSE_WEIGHT && moved.compareTo(BigDecimal.ZERO) < 0) {
            return scale(moved.abs().doubleValue() / weeks);
        }
        if (profile.getGoal() == Goal.GAIN_MASS && moved.compareTo(BigDecimal.ZERO) > 0) {
            return scale(moved.doubleValue() / weeks);
        }
        return null;
    }

    private static String resolvePaceStatus(int daysAheadOrBehind) {
        if (daysAheadOrBehind <= -7) {
            return "AHEAD";
        }
        if (daysAheadOrBehind <= 7) {
            return "ON_TRACK";
        }
        return "BEHIND";
    }

    private static String buildSummary(Goal goal,
                                       LocalDate targetDate,
                                       LocalDate projectedFinish,
                                       int daysAheadOrBehind,
                                       BigDecimal actualRate,
                                       BigDecimal requiredRate) {
        String rate = actualRate != null ? String.format("~%.2f", actualRate) : "—";
        String required = requiredRate != null ? String.format("~%.2f", requiredRate) : "—";
        if (daysAheadOrBehind <= -7) {
            return String.format(
                    "Ritmo atual %s kg/sem (meta %s). Previsão: %s — cerca de %d dias antes do prazo (%s).",
                    rate, required, formatDate(projectedFinish), Math.abs(daysAheadOrBehind), formatDate(targetDate));
        }
        if (daysAheadOrBehind <= 7) {
            return String.format(
                    "Ritmo atual %s kg/sem (meta %s). Previsão: %s — alinhado ao prazo (%s).",
                    rate, required, formatDate(projectedFinish), formatDate(targetDate));
        }
        return String.format(
                "Ritmo atual %s kg/sem (meta %s). Previsão: %s — cerca de %d dias após o prazo (%s). Vale revisar calorias ou prazo.",
                rate, required, formatDate(projectedFinish), daysAheadOrBehind, formatDate(targetDate));
    }

    private static String formatDate(LocalDate date) {
        return date != null ? date.toString() : "—";
    }

    private static BigDecimal scale(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
