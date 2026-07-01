package br.com.nutriplus.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GoalTimelineResponse(
        LocalDate journeyStartDate,
        LocalDate targetDate,
        BigDecimal startWeightKg,
        BigDecimal targetWeightKg,
        BigDecimal latestWeightKg,
        BigDecimal requiredRateKgPerWeek,
        BigDecimal actualRateKgPerWeek,
        LocalDate projectedFinishDate,
        String paceStatus,
        int daysAheadOrBehind,
        String summary,
        Long currentPlanId,
        LocalDate currentPlanStartDate,
        int previousPlanCount,
        List<GoalTimelineWeightPoint> weightHistory,
        List<GoalTimelinePlanEra> planEras,
        List<GoalTimelineChartPoint> requiredPaceLine,
        List<GoalTimelineChartPoint> projectionLine
) {
}
