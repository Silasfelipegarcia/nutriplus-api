package br.com.nutriplus.client.dto;

import java.math.BigDecimal;

public record AiMealItemDto(
        String foodName,
        BigDecimal quantityG,
        BigDecimal calories,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG
) {
}
