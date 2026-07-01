package br.com.nutriplus.client.dto;

import java.math.BigDecimal;

public record AiMealItemDto(
        String foodName,
        BigDecimal quantityG,
        String quantityDisplay,
        String unitKind,
        BigDecimal calories,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG
) {
}
