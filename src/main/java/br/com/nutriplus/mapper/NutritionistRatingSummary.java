package br.com.nutriplus.mapper;

import java.util.Map;

public record NutritionistRatingSummary(double averageStars, long count) {

    public static NutritionistRatingSummary empty() {
        return new NutritionistRatingSummary(0.0, 0L);
    }

    public static NutritionistRatingSummary fromRow(Object[] row) {
        Double avg = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
        long count = row[2] != null ? ((Number) row[2]).longValue() : 0L;
        return new NutritionistRatingSummary(avg, count);
    }

    public static NutritionistRatingSummary lookup(Map<Long, NutritionistRatingSummary> map, Long id) {
        return map.getOrDefault(id, empty());
    }
}
