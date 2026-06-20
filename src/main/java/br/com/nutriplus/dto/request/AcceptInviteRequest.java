package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AcceptInviteRequest(
        @NotNull Boolean consentDataSharing,
        @NotBlank String consentVersion
) {
}
