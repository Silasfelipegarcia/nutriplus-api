package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateSubscriptionPlanRequest(
        @NotBlank @Size(max = 128) String name,
        @Size(max = 512) String description,
        @Min(0) int priceCents,
        @Min(0) int periodDays,
        @Size(max = 16) String priceSuffix,
        List<@NotBlank @Size(max = 200) String> benefits,
        boolean trialAvailable,
        boolean contactSales,
        boolean enabled,
        boolean visibleInCatalog,
        int sortOrder
) {
}
