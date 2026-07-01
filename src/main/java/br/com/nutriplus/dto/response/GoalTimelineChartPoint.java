package br.com.nutriplus.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalTimelineChartPoint(
        LocalDate date,
        BigDecimal weightKg
) {
}
