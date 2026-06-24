package br.com.nutriplus.dto.response;

import java.util.List;

public record NutritionistRatingsSummaryResponse(
        double averageStars,
        long totalRatings,
        List<CareRatingResponse> recent
) {
}
