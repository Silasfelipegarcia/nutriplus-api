package br.com.nutriplus.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiMealItemDto {
    private String foodName;
    private BigDecimal quantityG;
    private BigDecimal calories;
    private BigDecimal proteinG;
    private BigDecimal carbsG;
    private BigDecimal fatG;
}
