package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.SubscriptionPlan;

import java.time.LocalDateTime;
import java.util.List;

public record AdminSubscriptionPlanResponse(
        Long id,
        SubscriptionPlan planCode,
        String name,
        String description,
        int priceCents,
        int periodDays,
        String priceSuffix,
        List<String> benefits,
        boolean trialAvailable,
        boolean contactSales,
        boolean enabled,
        boolean visibleInCatalog,
        int sortOrder,
        LocalDateTime updatedAt
) {
}
