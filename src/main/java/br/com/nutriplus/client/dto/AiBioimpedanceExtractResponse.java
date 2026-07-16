package br.com.nutriplus.client.dto;

import java.math.BigDecimal;
import java.util.List;

public record AiBioimpedanceExtractResponse(
        String measuredOn,
        BigDecimal weightKg,
        BigDecimal bodyFatPercent,
        BigDecimal muscleMassKg,
        BigDecimal waistCm,
        BigDecimal hipCm,
        BigDecimal chestCm,
        BigDecimal neckCm,
        BigDecimal armRightCm,
        BigDecimal armLeftCm,
        BigDecimal thighRightCm,
        BigDecimal thighLeftCm,
        BigDecimal manualBmrKcal,
        String calculationMethod,
        List<String> foundFields,
        List<String> missingFields,
        String confidence,
        String notesForUser
) {
}
