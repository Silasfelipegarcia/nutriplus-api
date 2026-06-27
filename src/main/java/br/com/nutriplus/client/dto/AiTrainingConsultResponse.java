package br.com.nutriplus.client.dto;

import java.util.List;

public record AiTrainingConsultResponse(
        String summary,
        List<String> details,
        List<String> warnings,
        List<String> suggestedActions
) {
}
