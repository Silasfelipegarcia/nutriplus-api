package br.com.nutriplus.dto.request;

import br.com.nutriplus.infrastructure.validation.ValidCpf;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "Informe seu nome") @Size(max = 150) String name,
        @NotBlank(message = "Informe seu e-mail") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "Informe sua senha") @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres") String password,
        @NotBlank @ValidCpf String cpf,
        @NotNull(message = "Informe sua data de nascimento") LocalDate birthDate,
        @NotBlank(message = "Informe seu telefone") @Size(max = 20) String contactPhone,
        @Size(max = 64) String acquisitionSource,
        @Size(max = 64) String acquisitionMedium,
        @Size(max = 128) String acquisitionCampaign,
        @Size(max = 128) String acquisitionLanding
) {
}
