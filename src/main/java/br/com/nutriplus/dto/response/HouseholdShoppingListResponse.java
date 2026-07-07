package br.com.nutriplus.dto.response;

import java.util.List;

public record HouseholdShoppingListResponse(
        Long householdId,
        List<AggregatedShoppingItemResponse> items
) {
}
