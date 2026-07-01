package br.com.nutriplus.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalTimelineWeightPoint(
        LocalDate date,
        BigDecimal weightKg,
        boolean currentPlanPeriod
) {
}
