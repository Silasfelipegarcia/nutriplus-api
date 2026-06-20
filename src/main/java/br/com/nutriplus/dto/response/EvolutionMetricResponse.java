package br.com.nutriplus.dto.response;

import java.math.BigDecimal;

public record EvolutionMetricResponse(
        String key,
        String label,
        String unit,
        BigDecimal baseline,
        BigDecimal current,
        BigDecimal target,
        BigDecimal delta,
        String direction,
        String status,
        String statusLabel,
        String insight
) {
}
