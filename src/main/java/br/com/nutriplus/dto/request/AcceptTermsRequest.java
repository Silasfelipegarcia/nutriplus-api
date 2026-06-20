package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AcceptTermsRequest(
        @NotBlank String termsVersion,
        @NotBlank String privacyVersion
) {
}
