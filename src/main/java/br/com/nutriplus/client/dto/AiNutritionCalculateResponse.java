package br.com.nutriplus.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiNutritionCalculateResponse {
    private BigDecimal bmrKcal;
    private BigDecimal tdeeKcal;
    private BigDecimal targetCalories;
    private BigDecimal targetProteinG;
    private BigDecimal targetCarbsG;
    private BigDecimal targetFatG;
}
