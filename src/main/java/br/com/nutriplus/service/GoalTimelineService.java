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
    private static final int MIN_DAYS_FOR_RATE = 7;
    private static final int PROJECTION_STEP_DAYS = 7;

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
        List<GoalTimelinePlanEra> planEras = buildPlanEras(plansDesc);

        if (profile.getGoal() == Goal.MAINTAIN_WEIGHT) {
            BigDecimal weight = resolveLatestWeight(user.getId(), profile, allMeasurements);
            List<GoalTimelineWeightPoint> weightHistory = buildWeightHistory(allMeasurements, planStart, weight, planStart);
            return maintainResponse(profile, currentPlanId, planStart, previousPlanCount, weightHistory, planEras);
        }

        Integer weeks = profile.getGoalTargetWeeks();
        if (weeks == null || weeks <= 0) {
            BigDecimal weight = resolveLatestWeight(user.getId(), profile, allMeasurements);
            List<GoalTimelineWeightPoint> weightHistory = buildWeightHistory(allMeasurements, planStart, weight, planStart);
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
        BigDecimal rawActualRate = resolveActualRate(
                profile.getGoal(), planStart, planMeasurements, latestWeight, startWeight);
        Double weightSigned = toSignedWeeklyKg(rawActualRate, profile.getGoal());

        int recentAdherenceDays = 7;
        CheckinAdherenceHistoryResponse recentAdherence =
                checkinService.getAdherenceHistory(recentAdherenceDays);
        Double calorieSigned = extractSignedWeeklyKg(recentAdherence);

        double weightBlend = computeWeightBlendWeight(planMeasurements, today);
        Double signedTrend = blendSignedTrend(weightSigned, calorieSigned, weightBlend);

        BigDecimal actualRate = signedTrend != null
                ? normalizeActualRate(
                        magnitudeTowardGoal(signedTrend, profile.getGoal()),
                        requiredRateBd,
                        profile.getGoal())
                : null;
        BigDecimal remainingKg = latestWeight.subtract(targetWeight).abs();

        List<GoalTimelineWeightPoint> weightHistory = buildWeightHistory(
                allMeasurements, planStart, startWeight, journeyStart);

        LocalDate projectedFinish = null;
        String paceStatus = "INSUFFICIENT_DATA";
        int daysAheadOrBehind = 0;
        String summary;

        if (signedTrend != null && signedTrend != 0 && remainingKg.compareTo(BigDecimal.ZERO) > 0.05) {
            BigDecimal towardGoalRate = magnitudeTowardGoal(signedTrend, profile.getGoal());
            if (towardGoalRate != null && towardGoalRate.compareTo(BigDecimal.ZERO) > 0) {
                projectedFinish = projectFinishDate(today, remainingKg, towardGoalRate);
                daysAheadOrBehind = (int) ChronoUnit.DAYS.between(targetDate, projectedFinish);
                paceStatus = resolvePaceStatus(daysAheadOrBehind);
                summary = buildSummary(profile.getGoal(), targetDate, projectedFinish, daysAheadOrBehind,
                        towardGoalRate, requiredRateBd, planStart, previousPlanCount);
                if (calorieSigned != null && (weightSigned == null || weightBlend < 0.5)) {
                    summary = summary + " Tendência influenciada pelas calorias recentes do plano.";
                } else if (calorieSigned != null && weightSigned != null) {
                    summary = summary + " Tendência combina peso e calorias recentes.";
                }
            } else {
                paceStatus = "BEHIND";
                daysAheadOrBehind = (int) ChronoUnit.DAYS.between(targetDate, today.plusMonths(2));
                summary = buildAdverseCalorieSummary(profile.getGoal(), planStart, previousPlanCount, calorieSigned);
            }
        } else if (actualRate != null && actualRate.compareTo(BigDecimal.ZERO) > 0
                && remainingKg.compareTo(BigDecimal.ZERO) > 0.05) {
            projectedFinish = projectFinishDate(today, remainingKg, actualRate);
            daysAheadOrBehind = (int) ChronoUnit.DAYS.between(targetDate, projectedFinish);
            paceStatus = resolvePaceStatus(daysAheadOrBehind);
            summary = buildSummary(profile.getGoal(), targetDate, projectedFinish, daysAheadOrBehind,
                    actualRate, requiredRateBd, planStart, previousPlanCount);
        } else {
            int adherenceDays = (int) Math.min(90, Math.max(7, ChronoUnit.DAYS.between(planStart, today) + 1));
            CheckinAdherenceHistoryResponse adherence = checkinService.getAdherenceHistory(adherenceDays);
            CheckinAdherenceProjectionResponse projection = adherence.projection();
            if (projection.estimatedWeightChangeKgPerWeek() != null
                    && projection.estimatedWeightChangeKgPerWeek() != 0
                    && remainingKg.compareTo(BigDecimal.ZERO) > 0.05) {
                signedTrend = projection.estimatedWeightChangeKgPerWeek();
                actualRate = normalizeActualRate(
                        magnitudeTowardGoal(signedTrend, profile.getGoal()),
                        requiredRateBd,
                        profile.getGoal());
                if (actualRate != null && actualRate.compareTo(BigDecimal.ZERO) > 0) {
                    projectedFinish = projectFinishDate(today, remainingKg, actualRate);
                    daysAheadOrBehind = (int) ChronoUnit.DAYS.between(targetDate, projectedFinish);
                    paceStatus = resolvePaceStatus(daysAheadOrBehind);
                    summary = buildSummary(profile.getGoal(), targetDate, projectedFinish, daysAheadOrBehind,
                            actualRate, requiredRateBd, planStart, previousPlanCount)
                            + " Ritmo estimado pelos check-ins do plano atual.";
                } else {
                    paceStatus = "BEHIND";
                    summary = buildAdverseCalorieSummary(profile.getGoal(), planStart, previousPlanCount, signedTrend);
                }
            } else {
                summary = buildInsufficientSummary(weeks, targetDate, planStart, previousPlanCount);
            }
        }

        LocalDate chartEndDate = resolveChartEndDate(journeyStart, targetDate, projectedFinish, daysAheadOrBehind);
        LocalDate trendStart = resolveTrendStart(today, planMeasurements);
        BigDecimal trendWeight = resolveTrendWeight(trendStart, today, latestWeight, planMeasurements);

        List<GoalTimelineChartPoint> requiredPaceLine = buildPaceLine(
                journeyStart, startWeight, targetDate, targetWeight, profile.getGoal());
        List<GoalTimelineChartPoint> projectionLine = buildProjectionLine(
                trendStart,
                trendWeight,
                signedTrend,
                targetWeight,
                profile.getGoal(),
                chartEndDate);

        return new GoalTimelineResponse(
                journeyStart,
                targetDate,
                chartEndDate,
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
                                                                    LocalDate planStart,
                                                                    BigDecimal planStartWeight,
                                                                    LocalDate journeyStart) {
        List<GoalTimelineWeightPoint> points = all.stream()
                .map(session -> new GoalTimelineWeightPoint(
                        session.getMeasuredOn(),
                        session.getWeightKg(),
                        !session.getMeasuredOn().isBefore(planStart)
                ))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        boolean hasPlanStartAnchor = points.stream()
                .anyMatch(p -> p.currentPlanPeriod() && p.date().equals(journeyStart));
        if (!hasPlanStartAnchor && planStartWeight != null) {
            points.add(new GoalTimelineWeightPoint(journeyStart, planStartWeight, true));
            points.sort(java.util.Comparator.comparing(GoalTimelineWeightPoint::date));
        }
        return List.copyOf(points);
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
            long days = ChronoUnit.DAYS.between(first.getMeasuredOn(), last.getMeasuredOn());
            if (days >= MIN_DAYS_FOR_RATE) {
                BigDecimal weightDelta = last.getWeightKg().subtract(first.getWeightKg());
                double weeks = days / 7.0;
                if (goal == Goal.LOSE_WEIGHT && weightDelta.compareTo(BigDecimal.ZERO) < 0) {
                    return scale(weightDelta.abs().doubleValue() / weeks);
                }
                if (goal == Goal.GAIN_MASS && weightDelta.compareTo(BigDecimal.ZERO) > 0) {
                    return scale(weightDelta.doubleValue() / weeks);
                }
            }
        }

        long daysSinceStart = ChronoUnit.DAYS.between(planStart, LocalDate.now());
        if (daysSinceStart < MIN_DAYS_FOR_RATE) {
            return null;
        }
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

    static BigDecimal normalizeActualRate(BigDecimal rawRate, BigDecimal requiredRate, Goal goal) {
        if (rawRate == null || rawRate.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        double required = requiredRate != null ? requiredRate.doubleValue() : 0.2;
        double cap = Math.max(required * 2.5, 0.15);
        if (goal == Goal.LOSE_WEIGHT) {
            cap = Math.min(cap, MAX_SAFE_LOSS_KG_PER_WEEK * 1.15);
        } else if (goal == Goal.GAIN_MASS) {
            cap = Math.min(cap, 0.75);
        }
        return scale(Math.min(rawRate.doubleValue(), cap));
    }

    static LocalDate resolveChartEndDate(LocalDate journeyStart,
                                         LocalDate targetDate,
                                         LocalDate projectedFinish,
                                         int daysAheadOrBehind) {
        LocalDate end = targetDate != null ? targetDate : journeyStart.plusWeeks(12);
        if (daysAheadOrBehind > 7 && projectedFinish != null && projectedFinish.isAfter(end)) {
            return projectedFinish.plusDays(PROJECTION_STEP_DAYS);
        }
        return end;
    }

    private static LocalDate resolveTrendStart(LocalDate today, List<BodyMeasurementSession> planMeasurements) {
        if (planMeasurements.isEmpty()) {
            return today;
        }
        BodyMeasurementSession last = planMeasurements.getLast();
        long daysSinceLast = ChronoUnit.DAYS.between(last.getMeasuredOn(), today);
        if (daysSinceLast <= 21) {
            return last.getMeasuredOn();
        }
        return today;
    }

    private static BigDecimal resolveTrendWeight(LocalDate trendStart,
                                                 LocalDate today,
                                                 BigDecimal latestWeight,
                                                 List<BodyMeasurementSession> planMeasurements) {
        if (!planMeasurements.isEmpty()) {
            BodyMeasurementSession last = planMeasurements.getLast();
            if (last.getMeasuredOn().equals(trendStart)) {
                return last.getWeightKg();
            }
        }
        return latestWeight;
    }

    private static LocalDate projectFinishDate(LocalDate today, BigDecimal remainingKg, BigDecimal actualRate) {
        double weeksToGo = remainingKg.doubleValue() / actualRate.doubleValue();
        return today.plusDays(Math.round(weeksToGo * 7));
    }

    static List<GoalTimelineChartPoint> buildPaceLine(LocalDate startDate,
                                                      BigDecimal startWeight,
                                                      LocalDate endDate,
                                                      BigDecimal endWeight,
                                                      Goal goal) {
        if (startDate == null || endDate == null || startWeight == null || endWeight == null) {
            return List.of();
        }
        long totalDays = Math.max(ChronoUnit.DAYS.between(startDate, endDate), 1);
        List<GoalTimelineChartPoint> points = new ArrayList<>();
        points.add(new GoalTimelineChartPoint(startDate, startWeight));

        for (long offset = PROJECTION_STEP_DAYS; offset < totalDays; offset += PROJECTION_STEP_DAYS) {
            LocalDate date = startDate.plusDays(offset);
            double ratio = (double) offset / totalDays;
            double start = startWeight.doubleValue();
            double end = endWeight.doubleValue();
            double value = start + (end - start) * ratio;
            points.add(new GoalTimelineChartPoint(date, scale(value)));
        }
        points.add(new GoalTimelineChartPoint(endDate, endWeight));
        return points;
    }

    static List<GoalTimelineChartPoint> buildProjectionLine(LocalDate trendStart,
                                                            BigDecimal trendWeight,
                                                            Double signedKgPerWeek,
                                                            BigDecimal targetWeight,
                                                            Goal goal,
                                                            LocalDate chartEndDate) {
        if (trendStart == null || trendWeight == null || chartEndDate == null) {
            return List.of();
        }
        List<GoalTimelineChartPoint> points = new ArrayList<>();
        points.add(new GoalTimelineChartPoint(trendStart, trendWeight));

        if (signedKgPerWeek == null || signedKgPerWeek == 0 || targetWeight == null) {
            if (!trendStart.equals(chartEndDate)) {
                points.add(new GoalTimelineChartPoint(chartEndDate, trendWeight));
            }
            return points;
        }

        double signed = signedKgPerWeek;
        double origin = trendWeight.doubleValue();
        double target = targetWeight.doubleValue();

        for (long offset = PROJECTION_STEP_DAYS; ; offset += PROJECTION_STEP_DAYS) {
            LocalDate date = trendStart.plusDays(offset);
            boolean isEnd = date.isAfter(chartEndDate);
            if (isEnd) {
                date = chartEndDate;
            }

            long daysFromStart = ChronoUnit.DAYS.between(trendStart, date);
            double weeks = daysFromStart / 7.0;
            double projected = origin + signed * weeks;

            if (goal == Goal.LOSE_WEIGHT && signed < 0) {
                projected = Math.max(projected, target);
            } else if (goal == Goal.GAIN_MASS && signed > 0) {
                projected = Math.min(projected, target);
            }

            BigDecimal projectedBd = scale(projected);

            GoalTimelineChartPoint last = points.get(points.size() - 1);
            if (!last.date().equals(date)) {
                points.add(new GoalTimelineChartPoint(date, projectedBd));
            }

            boolean reachedGoal = goal == Goal.GAIN_MASS
                    ? projected >= target - 0.01 && signed > 0
                    : projected <= target + 0.01 && signed < 0;
            if (reachedGoal) {
                points.set(points.size() - 1, new GoalTimelineChartPoint(date, targetWeight));
                if (date.isBefore(chartEndDate)) {
                    points.add(new GoalTimelineChartPoint(chartEndDate, targetWeight));
                }
                break;
            }
            if (isEnd) {
                break;
            }
        }
        return points;
    }

    static Double extractSignedWeeklyKg(CheckinAdherenceHistoryResponse adherence) {
        if (adherence == null || adherence.projection() == null) {
            return null;
        }
        return adherence.projection().estimatedWeightChangeKgPerWeek();
    }

    static Double toSignedWeeklyKg(BigDecimal magnitudeRate, Goal goal) {
        if (magnitudeRate == null || goal == null || goal == Goal.MAINTAIN_WEIGHT) {
            return null;
        }
        double value = magnitudeRate.doubleValue();
        return goal == Goal.GAIN_MASS ? value : -value;
    }

    static double computeWeightBlendWeight(List<BodyMeasurementSession> planMeasurements, LocalDate today) {
        if (planMeasurements.isEmpty()) {
            return 0.15;
        }
        long daysSinceLast = ChronoUnit.DAYS.between(planMeasurements.getLast().getMeasuredOn(), today);
        if (daysSinceLast <= 3) {
            return 0.55;
        }
        if (daysSinceLast <= 7) {
            return 0.45;
        }
        if (daysSinceLast <= 14) {
            return 0.30;
        }
        if (daysSinceLast <= 21) {
            return 0.20;
        }
        return 0.10;
    }

    static Double blendSignedTrend(Double weightSigned, Double calorieSigned, double weightBlend) {
        if (weightSigned == null && calorieSigned == null) {
            return null;
        }
        if (weightSigned == null) {
            return calorieSigned;
        }
        if (calorieSigned == null) {
            return weightSigned;
        }
        double w = Math.clamp(weightBlend, 0.0, 1.0);
        return w * weightSigned + (1.0 - w) * calorieSigned;
    }

    static BigDecimal magnitudeTowardGoal(double signedKgPerWeek, Goal goal) {
        if (goal == Goal.LOSE_WEIGHT) {
            return signedKgPerWeek < 0 ? scale(-signedKgPerWeek) : scale(0);
        }
        if (goal == Goal.GAIN_MASS) {
            return signedKgPerWeek > 0 ? scale(signedKgPerWeek) : scale(0);
        }
        return null;
    }

    private static String buildAdverseCalorieSummary(Goal goal,
                                                     LocalDate planStart,
                                                     int previousPlanCount,
                                                     Double signedTrend) {
        String prefix = planContextPrefix(planStart, previousPlanCount);
        if (goal == Goal.LOSE_WEIGHT && signedTrend != null && signedTrend > 0) {
            return prefix + "Calorias acima da meta nos últimos dias — a tendência aponta ganho de peso, não perda.";
        }
        if (goal == Goal.GAIN_MASS && signedTrend != null && signedTrend < 0) {
            return prefix + "Calorias abaixo da meta nos últimos dias — a tendência aponta perda de peso, não ganho.";
        }
        return prefix + "Ajuste aderência ao plano para alinhar a tendência com sua meta.";
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
