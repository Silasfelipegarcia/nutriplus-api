package br.com.nutriplus.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

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
        String summary
) {
}
