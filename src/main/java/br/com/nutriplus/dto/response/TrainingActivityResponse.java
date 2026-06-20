package br.com.nutriplus.dto.response;

import java.math.BigDecimal;

public record TrainingActivityResponse(
        Long id,
        String sportType,
        String label,
        int daysPerWeek,
        int minutesPerSession,
        BigDecimal caloriesPerSession,
        BigDecimal caloriesPerWeek
) {
}
