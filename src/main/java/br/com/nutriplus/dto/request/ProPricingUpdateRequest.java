package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProPricingUpdateRequest(
        @NotNull @Min(1) Integer consultationPriceCents,
        @Min(1) Integer careDurationDays
) {
}
