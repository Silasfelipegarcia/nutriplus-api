package br.com.nutriplus.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record EvolutionReportResponse(
        boolean hasMeasurements,
        String goal,
        BigDecimal targetWeightKg,
        BodyMeasurementResponse baseline,
        BodyMeasurementResponse latest,
        List<BodyMeasurementResponse> history,
        List<EvolutionMetricResponse> metrics,
        int excellentCount,
        int goodCount,
        int okCount,
        int belowCount,
        String headline,
        Integer weekAdherencePercent,
        Integer currentStreak,
        ProgressReviewResponse latestReview
) {
}
