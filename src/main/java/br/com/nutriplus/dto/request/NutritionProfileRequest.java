package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class NutritionProfileRequest {

    @NotNull
    @Min(10)
    @Max(120)
    private Integer age;

    @NotNull
    private Sex sex;

    @NotNull
    @DecimalMin("100.0")
    @DecimalMax("250.0")
    private BigDecimal heightCm;

    @NotNull
    @DecimalMin("30.0")
    @DecimalMax("300.0")
    private BigDecimal currentWeightKg;

    @NotNull
    @DecimalMin("30.0")
    @DecimalMax("300.0")
    private BigDecimal targetWeightKg;

    @NotNull
    private Goal goal;

    @NotNull
    private ActivityLevel activityLevel;

    @NotNull
    private DietaryPreference dietaryPreference;

    @NotNull
    private Restriction restriction;
}
