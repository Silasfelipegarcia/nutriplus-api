package br.com.nutriplus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealItemResponse {
    private Long id;
    private String foodName;
    private BigDecimal quantityG;
    private BigDecimal calories;
    private BigDecimal proteinG;
    private BigDecimal carbsG;
    private BigDecimal fatG;
}
