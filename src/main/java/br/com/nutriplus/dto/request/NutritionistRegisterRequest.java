package br.com.nutriplus.dto.request;

import br.com.nutriplus.infrastructure.validation.ValidCpf;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NutritionistRegisterRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @ValidCpf String cpf,
        @NotBlank @Size(max = 20) String contactPhone,
        @NotBlank @Size(max = 20) String crn,
        @Size(max = 2000) String bio,
        @Size(max = 500) String specialties
) {
}
