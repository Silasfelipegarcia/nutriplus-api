package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.BodyMeasurementSession;
import br.com.nutriplus.domain.entity.MealPlan;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.Goal;
import br.com.nutriplus.dto.response.CheckinAdherenceHistoryResponse;
import br.com.nutriplus.dto.response.CheckinAdherenceProjectionResponse;
import br.com.nutriplus.dto.response.GoalTimelineChartPoint;
import br.com.nutriplus.dto.response.GoalTimelinePlanEra;
import br.com.nutriplus.dto.response.GoalTimelineResponse;
import br.com.nutriplus.dto.response.GoalTimelineWeightPoint;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GoalTimelineService {

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

        List<MealPlan> plansDesc = mealPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        MealPlan currentPlan = plansDesc.isEmpty() ? null : plansDesc.getFirst();
        LocalDate planStart = currentPlan != null
                ? currentPlan.getPlanDate()
                : (profile.getUpdatedAt() != null ? profile.getUpdatedAt().toLocalDate() : LocalDate.now());
        Long currentPlanId = currentPlan != null ? currentPlan.getId() : null;
        int previousPlanCount = Math.max(0, plansDesc.size() - 1);

        List<BodyMeasurementSession> allMeasurements =
                measurementRepository.findByUserIdOrderByMeasuredOnAscIdAsc(user.getId());
        List<GoalTimelineWeightPoint> weightHistory = buildWeightHistory(allMeasurements, planStart);
        List<GoalTimelinePlanEra> planEras = buildPlanEras(plansDesc);

        if (profile.getGoal() == Goal.MAINTAIN_WEIGHT) {
            return maintainResponse(profile, currentPlanId, planStart, previousPlanCount, weightHistory, planEras);
        }

        Integer weeks = profile.getGoalTargetWeeks();
        if (weeks == null || weeks <= 0) {
            return insufficientData(
                    profile,
                    "Defina um prazo em semanas no seu perfil para ver a previsão da meta.",
                    currentPlanId,
                    planStart,
                    previousPlanCount,
                    weightHistory,
                    planEras
            );
        }

        LocalDate journeyStart = planStart;
        LocalDate targetDate = journeyStart.plusWeeks(weeks);
        LocalDate today = LocalDate.now();

        BigDecimal startWeight = resolvePlanStartWeight(profile, planStart, allMeasurements);
        BigDecimal targetWeight = profile.getTargetWeightKg();
        BigDecimal latestWeight = resolveLatestWeight(user.getId(), profile, allMeasurements);
        BigDecimal deltaTotal = startWeight.subtract(targetWeight).abs();

        double requiredRate = deltaTotal.doubleValue() / weeks;
        if (profile.getGoal() == Goal.LOSE_WEIGHT) {
            requiredRate = Math.min(requiredRate, MAX_SAFE_LOSS_KG_PER_WEEK);
        }
        BigDecimal requiredRateBd = scale(requiredRate);

        List<BodyMeasurementSession> planMeasurements = filterFromDate(allMeasurements, planStart);
        BigDecimal actualRate = resolveActualRate(profile.getGoal(), planStart, planMeasurements, latestWeight, startWeight);
        BigDecimal remainingKg = latestWeight.subtract(targetWeight).abs();

        LocalDate projectedFinish = null;
        String paceStatus = "INSUFFICIENT_DATA";
        int daysAheadOrBehind = 0;
        String summary;

        if (actualRate != null && actualRate.compareTo(BigDecimal.ZERO) > 0 && remainingKg.compareTo(BigDecimal.ZERO) > 0.05) {
            projectedFinish = projectFinishDate(today, remainingKg, actualRate);
            daysAheadOrBehind = (int) ChronoUnit.DAYS.between(targetDate, projectedFinish);
            paceStatus = resolvePaceStatus(daysAheadOrBehind);
            summary = buildSummary(profile.getGoal(), targetDate, projectedFinish, daysAheadOrBehind, actualRate, requiredRateBd, planStart, previousPlanCount);
        } else {
            int adherenceDays = (int) Math.min(90, Math.max(7, ChronoUnit.DAYS.between(planStart, today) + 1));
            CheckinAdherenceHistoryResponse adherence = checkinService.getAdherenceHistory(adherenceDays);
            CheckinAdherenceProjectionResponse projection = adherence.projection();
            if (projection.estimatedWeightChangeKgPerWeek() != null
                    && projection.estimatedWeightChangeKgPerWeek() != 0
                    && remainingKg.compareTo(BigDecimal.ZERO) > 0.05) {
                actualRate = scale(Math.abs(projection.estimatedWeightChangeKgPerWeek()));
                projectedFinish = projectFinishDate(today, remainingKg, actualRate);
                daysAheadOrBehind = (int) ChronoUnit.DAYS.between(targetDate, projectedFinish);
                paceStatus = resolvePaceStatus(daysAheadOrBehind);
                summary = buildSummary(profile.getGoal(), targetDate, projectedFinish, daysAheadOrBehind, actualRate, requiredRateBd, planStart, previousPlanCount)
                        + " Ritmo estimado pelos check-ins do plano atual.";
            } else {
                summary = buildInsufficientSummary(weeks, targetDate, planStart, previousPlanCount);
            }
        }

        List<GoalTimelineChartPoint> requiredPaceLine = buildPaceLine(
                journeyStart, startWeight, targetDate, targetWeight, profile.getGoal());
        List<GoalTimelineChartPoint> projectionLine = buildProjectionLine(
                today, latestWeight, projectedFinish, targetWeight);

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
                summary,
                currentPlanId,
                planStart,
                previousPlanCount,
                weightHistory,
                planEras,
                requiredPaceLine,
                projectionLine
        );
    }

    private GoalTimelineResponse maintainResponse(NutritionProfile profile,
                                                  Long currentPlanId,
                                                  LocalDate planStart,
                                                  int previousPlanCount,
                                                  List<GoalTimelineWeightPoint> weightHistory,
                                                  List<GoalTimelinePlanEra> planEras) {
        return new GoalTimelineResponse(
                planStart,
                null,
                profile.getCurrentWeightKg(),
                profile.getTargetWeightKg(),
                profile.getCurrentWeightKg(),
                BigDecimal.ZERO,
                null,
                null,
                "MAINTAIN",
                0,
                planContextPrefix(planStart, previousPlanCount)
                        + "Seu objetivo é manutenção — acompanhe aderência ao plano e medidas corporais.",
                currentPlanId,
                planStart,
                previousPlanCount,
                weightHistory,
                planEras,
                List.of(),
                List.of()
        );
    }

    private GoalTimelineResponse insufficientData(NutritionProfile profile,
                                                  String message,
                                                  Long currentPlanId,
                                                  LocalDate planStart,
                                                  int previousPlanCount,
                                                  List<GoalTimelineWeightPoint> weightHistory,
                                                  List<GoalTimelinePlanEra> planEras) {
        return new GoalTimelineResponse(
                planStart,
                null,
                profile.getCurrentWeightKg(),
                profile.getTargetWeightKg(),
                profile.getCurrentWeightKg(),
                null,
                null,
                null,
                "INSUFFICIENT_DATA",
                0,
                planContextPrefix(planStart, previousPlanCount) + message,
                currentPlanId,
                planStart,
                previousPlanCount,
                weightHistory,
                planEras,
                List.of(),
                List.of()
        );
    }

    private static List<GoalTimelineWeightPoint> buildWeightHistory(List<BodyMeasurementSession> all,
                                                                    LocalDate planStart) {
        return all.stream()
                .map(session -> new GoalTimelineWeightPoint(
                        session.getMeasuredOn(),
                        session.getWeightKg(),
                        !session.getMeasuredOn().isBefore(planStart)
                ))
                .toList();
    }

    private static List<GoalTimelinePlanEra> buildPlanEras(List<MealPlan> plansDesc) {
        if (plansDesc.isEmpty()) {
            return List.of();
        }
        List<MealPlan> asc = new ArrayList<>(plansDesc);
        Collections.reverse(asc);
        MealPlan current = plansDesc.getFirst();
        return asc.stream()
                .map(plan -> new GoalTimelinePlanEra(
                        plan.getId(),
                        plan.getPlanDate(),
                        plan.getId().equals(current.getId())
                ))
                .toList();
    }

    private static List<BodyMeasurementSession> filterFromDate(List<BodyMeasurementSession> all, LocalDate from) {
        return all.stream()
                .filter(session -> !session.getMeasuredOn().isBefore(from))
                .toList();
    }

    private BigDecimal resolvePlanStartWeight(NutritionProfile profile,
                                              LocalDate planStart,
                                              List<BodyMeasurementSession> all) {
        for (BodyMeasurementSession session : all) {
            if (!session.getMeasuredOn().isBefore(planStart)) {
                return session.getWeightKg();
            }
        }
        for (int i = all.size() - 1; i >= 0; i--) {
            if (all.get(i).getMeasuredOn().isBefore(planStart)) {
                return all.get(i).getWeightKg();
            }
        }
        return profile.getCurrentWeightKg();
    }

    private BigDecimal resolveLatestWeight(Long userId,
                                           NutritionProfile profile,
                                           List<BodyMeasurementSession> all) {
        if (!all.isEmpty()) {
            return all.getLast().getWeightKg();
        }
        return measurementRepository.findFirstByUserIdOrderByMeasuredOnDescIdDesc(userId)
                .map(BodyMeasurementSession::getWeightKg)
                .orElse(profile.getCurrentWeightKg());
    }

    private BigDecimal resolveActualRate(Goal goal,
                                         LocalDate planStart,
                                         List<BodyMeasurementSession> planMeasurements,
                                         BigDecimal latestWeight,
                                         BigDecimal startWeight) {
        if (planMeasurements.size() >= 2) {
            BodyMeasurementSession first = planMeasurements.getFirst();
            BodyMeasurementSession last = planMeasurements.getLast();
            long days = Math.max(ChronoUnit.DAYS.between(first.getMeasuredOn(), last.getMeasuredOn()), 1);
            BigDecimal weightDelta = last.getWeightKg().subtract(first.getWeightKg());
            double weeks = days / 7.0;
            if (goal == Goal.LOSE_WEIGHT && weightDelta.compareTo(BigDecimal.ZERO) < 0) {
                return scale(weightDelta.abs().doubleValue() / weeks);
            }
            if (goal == Goal.GAIN_MASS && weightDelta.compareTo(BigDecimal.ZERO) > 0) {
                return scale(weightDelta.doubleValue() / weeks);
            }
        }

        long daysSinceStart = Math.max(ChronoUnit.DAYS.between(planStart, LocalDate.now()), 1);
        double weeks = daysSinceStart / 7.0;
        BigDecimal moved = latestWeight.subtract(startWeight);
        if (goal == Goal.LOSE_WEIGHT && moved.compareTo(BigDecimal.ZERO) < 0) {
            return scale(moved.abs().doubleValue() / weeks);
        }
        if (goal == Goal.GAIN_MASS && moved.compareTo(BigDecimal.ZERO) > 0) {
            return scale(moved.doubleValue() / weeks);
        }
        return null;
    }

    private static LocalDate projectFinishDate(LocalDate today, BigDecimal remainingKg, BigDecimal actualRate) {
        double weeksToGo = remainingKg.doubleValue() / actualRate.doubleValue();
        return today.plusDays(Math.round(weeksToGo * 7));
    }

    private static List<GoalTimelineChartPoint> buildPaceLine(LocalDate startDate,
                                                            BigDecimal startWeight,
                                                            LocalDate endDate,
                                                            BigDecimal endWeight,
                                                            Goal goal) {
        if (startDate == null || endDate == null || startWeight == null || endWeight == null) {
            return List.of();
        }
        long totalDays = Math.max(ChronoUnit.DAYS.between(startDate, endDate), 1);
        List<GoalTimelineChartPoint> points = new ArrayList<>();
        for (int step = 0; step <= 4; step++) {
            long offset = (totalDays * step) / 4;
            LocalDate date = startDate.plusDays(offset);
            double ratio = totalDays == 0 ? 1 : (double) offset / totalDays;
            double start = startWeight.doubleValue();
            double end = endWeight.doubleValue();
            double value = goal == Goal.GAIN_MASS
                    ? start + (end - start) * ratio
                    : start + (end - start) * ratio;
            points.add(new GoalTimelineChartPoint(date, scale(value)));
        }
        return points;
    }

    private static List<GoalTimelineChartPoint> buildProjectionLine(LocalDate today,
                                                                    BigDecimal latestWeight,
                                                                    LocalDate projectedFinish,
                                                                    BigDecimal targetWeight) {
        if (today == null || projectedFinish == null || latestWeight == null || targetWeight == null) {
            return List.of();
        }
        return List.of(
                new GoalTimelineChartPoint(today, latestWeight),
                new GoalTimelineChartPoint(projectedFinish, targetWeight)
        );
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
                                       BigDecimal requiredRate,
                                       LocalDate planStart,
                                       int previousPlanCount) {
        String prefix = planContextPrefix(planStart, previousPlanCount);
        String rate = actualRate != null ? String.format("~%.2f", actualRate) : "—";
        String required = requiredRate != null ? String.format("~%.2f", requiredRate) : "—";
        if (daysAheadOrBehind <= -7) {
            return prefix + String.format(
                    "Ritmo atual %s kg/sem (meta %s). Previsão: %s — cerca de %d dias antes do prazo (%s).",
                    rate, required, formatDate(projectedFinish), Math.abs(daysAheadOrBehind), formatDate(targetDate));
        }
        if (daysAheadOrBehind <= 7) {
            return prefix + String.format(
                    "Ritmo atual %s kg/sem (meta %s). Previsão: %s — alinhado ao prazo (%s).",
                    rate, required, formatDate(projectedFinish), formatDate(targetDate));
        }
        return prefix + String.format(
                "Ritmo atual %s kg/sem (meta %s). Previsão: %s — cerca de %d dias após o prazo (%s).",
                rate, required, formatDate(projectedFinish), daysAheadOrBehind, formatDate(targetDate));
    }

    private static String buildInsufficientSummary(int weeks,
                                                   LocalDate targetDate,
                                                   LocalDate planStart,
                                                   int previousPlanCount) {
        return planContextPrefix(planStart, previousPlanCount)
                + "Registre refeições e peso no plano atual para estimar se você chega na meta em "
                + weeks + " semanas (até " + formatDate(targetDate) + ").";
    }

    private static String planContextPrefix(LocalDate planStart, int previousPlanCount) {
        if (previousPlanCount > 0) {
            return "Novo plano desde " + formatDate(planStart) + ". ";
        }
        return "Plano desde " + formatDate(planStart) + ". ";
    }

    private static String formatDate(LocalDate date) {
        return date != null ? date.toString() : "—";
    }

    private static BigDecimal scale(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
