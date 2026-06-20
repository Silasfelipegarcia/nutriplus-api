package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NutritionistRegisterRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 20) String crn,
        @Size(max = 2000) String bio,
        @Size(max = 500) String specialties
) {
}
