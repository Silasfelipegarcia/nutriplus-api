package br.com.nutriplus.dto.response;

import java.util.List;

public record ShoppingSwapOptionResponse(
        String id,
        String label,
        String costTier,
        String whyCheaper,
        String proteinLeanness,
        Integer kcalEstimate,
        List<String> matchesMealFoods
) {
}
