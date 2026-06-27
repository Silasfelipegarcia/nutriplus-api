package br.com.nutriplus.dto.response;

import java.util.List;

public record CoachInsightResponse(
        String summary,
        List<String> details,
        List<String> warnings,
        List<String> suggestedActions
) {
}
