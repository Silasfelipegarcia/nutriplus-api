package br.com.nutriplus.dto.response;

import java.math.BigDecimal;

public record HealthIndicatorResponse(
        String key,
        String label,
        BigDecimal value,
        String unit,
        String classification,
        String riskLevel,
        BigDecimal referenceMin,
        BigDecimal referenceMax,
        String referenceSource,
        String healthNote,
        BigDecimal baselineValue,
        BigDecimal deltaSinceBaseline,
        String deltaDirection
) {
}
