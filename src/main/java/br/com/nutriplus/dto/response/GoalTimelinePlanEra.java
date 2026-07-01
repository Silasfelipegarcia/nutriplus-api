package br.com.nutriplus.dto.response;

import java.time.LocalDate;

public record GoalTimelinePlanEra(
        Long planId,
        LocalDate startDate,
        boolean current
) {
}
