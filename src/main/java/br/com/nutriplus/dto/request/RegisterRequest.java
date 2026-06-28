package br.com.nutriplus.dto.request;

import br.com.nutriplus.infrastructure.validation.ValidCpf;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotBlank @ValidCpf String cpf,
        @NotNull LocalDate birthDate,
        @Size(max = 64) String acquisitionSource,
        @Size(max = 64) String acquisitionMedium,
        @Size(max = 128) String acquisitionCampaign,
        @Size(max = 128) String acquisitionLanding
) {
}
