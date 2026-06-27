package br.com.nutriplus.dto.response;

public record CheckinAdherenceProjectionResponse(
        int averageDailyCalorieDelta,
        Double estimatedWeightChangeKgPerWeek,
        String summary
) {
}
