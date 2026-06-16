package br.com.nutriplus.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AiMealPlanGenerateResponse {
    private String aiModel;
    private BigDecimal totalCalories;
    private BigDecimal totalProteinG;
    private BigDecimal totalCarbsG;
    private BigDecimal totalFatG;
    private List<AiMealDto> meals;
    private List<AiShoppingItemDto> shoppingList;
}
