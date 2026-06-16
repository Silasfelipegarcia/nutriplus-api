package br.com.nutriplus.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiMealDto {
    private String mealType;
    private String name;
    private Integer sortOrder;
    private List<AiMealItemDto> items;
}
