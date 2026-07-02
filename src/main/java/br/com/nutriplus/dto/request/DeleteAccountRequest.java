package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(
        @NotBlank String currentPassword,
        @NotBlank String emailConfirmation
) {
}
