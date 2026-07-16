package br.com.nutriplus.dto.response;

import java.util.List;
import java.util.Map;

public record CycleBehaviorSignalsResponse(
        int extraCaloriesTotal,
        int extraEntriesCount,
        int daysWithExtras,
        int equivalentSwapsCount,
        Map<String, Integer> extrasByMealType,
        List<String> topExtraDescriptions,
        String highlight
) {
}
