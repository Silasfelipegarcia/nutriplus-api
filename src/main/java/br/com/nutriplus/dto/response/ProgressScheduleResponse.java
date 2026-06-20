package br.com.nutriplus.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProgressScheduleResponse(
        int intervalDays,
        boolean due,
        int daysUntilDue,
        LocalDate nextDueOn,
        LocalDateTime lastReviewAt,
        LocalDate lastMeasurementOn
) {
}
