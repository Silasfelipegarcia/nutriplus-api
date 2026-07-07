package br.com.nutriplus.dto.response;

public record AggregatedShoppingItemResponse(
        String itemName,
        String quantity,
        String category,
        int memberCount
) {
}
