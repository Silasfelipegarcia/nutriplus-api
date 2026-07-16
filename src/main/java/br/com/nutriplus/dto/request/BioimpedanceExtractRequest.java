package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BioimpedanceExtractRequest(
        @NotBlank @Size(max = 40) String mimeType,
        @NotBlank @Size(min = 16, max = 12_000_000) String contentBase64
) {
}
