package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.CalculationMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProBodyMeasurementRequest(
        @NotNull CalculationMethod calculationMethod,
        @NotNull LocalDate measuredOn,
        @NotNull @Positive BigDecimal weightKg,
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
    public BodyMeasurementRequest toBodyMeasurementRequest() {
        return new BodyMeasurementRequest(
                measuredOn,
                weightKg,
                bodyFatPercent,
                muscleMassKg,
                waistCm,
                hipCm,
                chestCm,
                neckCm,
                armRightCm,
                armLeftCm,
                thighRightCm,
                thighLeftCm,
                notes
        );
    }
}
