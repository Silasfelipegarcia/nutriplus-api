package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BodyMeasurementRequest(
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
        String notes,
        /** Quando preenchido (ex.: BIOIMPEDANCE após laudo), atualiza o perfil. */
        String calculationMethod
) {
    public BodyMeasurementRequest(
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
        this(
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
                notes,
                null
        );
    }
}
