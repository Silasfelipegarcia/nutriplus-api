package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateFeatureFlagRequest(
        @NotNull Boolean enabled
) {
}
