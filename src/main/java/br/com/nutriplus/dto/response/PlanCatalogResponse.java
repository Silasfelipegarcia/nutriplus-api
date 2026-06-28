package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.SubscriptionPlan;

import java.util.List;

public record PlanCatalogResponse(
        boolean billingEnabled,
        List<PlanCatalogItemResponse> plans
) {
}
