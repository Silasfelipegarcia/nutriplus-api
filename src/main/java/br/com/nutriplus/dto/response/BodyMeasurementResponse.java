package br.com.nutriplus.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BodyMeasurementResponse(
        Long id,
        LocalDate measuredOn,
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
        String notes
) {
}
