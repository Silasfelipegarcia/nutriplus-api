package br.com.nutriplus.client.dto;

import java.util.List;

public record AiShoppingSwapOptionDto(
        String id,
        String label,
        String costTier,
        String whyCheaper,
        String proteinLeanness,
        Integer kcalEstimate,
        List<String> matchesMealFoods
) {
}
