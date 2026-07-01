package br.com.nutriplus.client.dto;

import java.math.BigDecimal;

public record AiFoodExtraEstimateResponse(
        int estimatedCalories,
        BigDecimal estimatedCarbsG,
        String impactMessage
) {
}
