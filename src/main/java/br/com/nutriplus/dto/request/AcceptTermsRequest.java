package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AcceptTermsRequest(
        @NotBlank String termsVersion,
        @NotBlank String privacyVersion,
        @NotNull Boolean healthEligibilityAccepted,
        @NotBlank String healthEligibilityVersion,
        String appPlatform
) {
}
