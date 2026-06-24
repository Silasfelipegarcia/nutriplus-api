package br.com.nutriplus.dto.response;

public record NutritionistPendingResponse(
        Long nutritionistId,
        String name,
        String email,
        String crn,
        String cpfMasked,
        boolean marketplaceVisible
) {
}
