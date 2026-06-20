package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;

public record AppFeedbackResponse(
        Long id,
        LocalDateTime submittedAt,
        String message,
        Integer easeOfUse,
        Integer mealPlanQuality,
        Integer aiHelpfulness,
        Integer progressTracking,
        Integer overallSatisfaction,
        String improvementSuggestions,
        String appVersion,
        String platform
) {
}
