package br.com.nutriplus.client.dto;

import java.util.List;

public record AiProgressAnalyzeResponse(
        String trend,
        String summary,
        List<String> recommendations,
        String confidence,
        Boolean planChangeSuggested,
        String planChangeRationale,
        String keepPlanMessage
) {
}
