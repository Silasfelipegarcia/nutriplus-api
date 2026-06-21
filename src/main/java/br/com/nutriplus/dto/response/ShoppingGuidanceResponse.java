package br.com.nutriplus.dto.response;

import java.util.List;

public record ShoppingGuidanceResponse(
        List<SatietyTipResponse> satietyTips,
        List<FlexibleOptionResponse> flexibleOptions,
        String weeklyImpactSummary,
        String budgetSummary,
        String routineCurrentSummary,
        String routineSuggestedSummary,
        String routineGentleNote
) {
}
