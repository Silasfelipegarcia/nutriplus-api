package br.com.nutriplus.client.dto;

public record AiFoodExtraEstimateResponse(
        int estimatedCalories,
        String impactMessage
) {
}
