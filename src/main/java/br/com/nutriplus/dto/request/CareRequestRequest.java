package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.PreferredCareMode;
import jakarta.validation.constraints.NotNull;

public record CareRequestRequest(
        PreferredCareMode preferredCareMode,
        @NotNull Boolean consentDataSharing
) {
}
