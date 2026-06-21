package br.com.nutriplus.client.dto;

import java.util.List;

public record AiShoppingGuidanceDto(
        List<AiSatietyTipDto> satietyTips,
        List<AiFlexibleOptionDto> flexibleOptions,
        String weeklyImpactSummary,
        String budgetSummary
) {
}
