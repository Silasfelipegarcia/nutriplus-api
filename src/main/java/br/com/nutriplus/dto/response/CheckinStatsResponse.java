package br.com.nutriplus.dto.response;

public record CheckinStatsResponse(
        int streakDays,
        int weekAdherencePercent
) {
}
