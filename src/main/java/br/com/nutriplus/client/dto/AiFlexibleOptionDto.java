package br.com.nutriplus.client.dto;

public record AiFlexibleOptionDto(
        String id,
        String label,
        Integer kcalEstimate,
        String impactSummary,
        Double deficitPercent,
        Double daysDelay,
        String evandroStatus,
        String evandroNote
) {
}
