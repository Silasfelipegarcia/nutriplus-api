package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionProfileResponse {
    private Long id;
    private Integer age;
    private Sex sex;
    private BigDecimal heightCm;
    private BigDecimal currentWeightKg;
    private BigDecimal targetWeightKg;
    private Goal goal;
    private ActivityLevel activityLevel;
    private DietaryPreference dietaryPreference;
    private Restriction restriction;
    private BigDecimal bmrKcal;
    private BigDecimal tdeeKcal;
    private BigDecimal targetCalories;
    private BigDecimal targetProteinG;
    private BigDecimal targetCarbsG;
    private BigDecimal targetFatG;
    private LocalDateTime updatedAt;
}
