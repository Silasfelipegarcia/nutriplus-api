package br.com.nutriplus.dto.response;

import java.math.BigDecimal;

public record MealItemResponse(
        Long id,
        String foodName,
        BigDecimal quantityG,
        BigDecimal calories,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG
) {
}
