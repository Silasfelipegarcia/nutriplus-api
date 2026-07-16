package br.com.nutriplus.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record BioimpedanceExtractResponse(
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
