package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanResponse {
    private Long id;
    private LocalDate planDate;
    private BigDecimal totalCalories;
    private BigDecimal totalProteinG;
    private BigDecimal totalCarbsG;
    private BigDecimal totalFatG;
    private String disclaimer;
    private List<MealResponse> meals;
    private LocalDateTime createdAt;
}
