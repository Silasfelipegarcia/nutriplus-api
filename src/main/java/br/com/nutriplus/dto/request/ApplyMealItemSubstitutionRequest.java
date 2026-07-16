package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ApplyMealItemSubstitutionRequest(
        @NotBlank String foodName,
        @NotNull @Positive BigDecimal quantityG,
        String quantityDisplay,
        String unitKind,
        BigDecimal calories,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG
) {
}
