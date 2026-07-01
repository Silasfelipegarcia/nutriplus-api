package br.com.nutriplus.dto.response;

public record NutritionistPortfolioItemResponse(
        Long id,
        String title,
        String summary,
        int sortOrder
) {
}
