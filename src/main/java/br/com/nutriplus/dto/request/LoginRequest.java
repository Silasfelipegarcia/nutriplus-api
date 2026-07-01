package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Informe seu e-mail") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "Informe sua senha") String password
) {
}
