package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProgressReviewResponse(
        Long id,
        String status,
        String trend,
        String summary,
        List<String> recommendations,
        Integer weekAdherencePercent,
        BodyMeasurementResponse current,
        BodyMeasurementResponse previous,
        LocalDateTime completedAt,
        Boolean planChangeSuggested,
        String planChangeRationale,
        String keepPlanMessage,
        String confidence,
        CycleBehaviorSignalsResponse cycleBehavior
) {
}
