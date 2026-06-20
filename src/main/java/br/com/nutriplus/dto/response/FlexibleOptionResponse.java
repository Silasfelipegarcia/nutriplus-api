package br.com.nutriplus.dto.response;

public record FlexibleOptionResponse(
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
