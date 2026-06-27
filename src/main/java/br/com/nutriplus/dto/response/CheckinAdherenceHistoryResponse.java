package br.com.nutriplus.dto.response;

import java.util.List;

public record CheckinAdherenceHistoryResponse(
        int windowDays,
        int overallAdherencePercent,
        int streakDays,
        Integer targetCalories,
        String goal,
        List<DailyAdherenceResponse> daily,
        CheckinAdherenceProjectionResponse projection
) {
}
