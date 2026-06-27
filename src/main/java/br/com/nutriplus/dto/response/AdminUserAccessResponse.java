package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.RegistrationSource;
import br.com.nutriplus.domain.enums.UserRole;

import java.time.LocalDateTime;

public record AdminUserAccessResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        boolean loginEnabled,
        LocalDateTime loginEnabledAt,
        LocalDateTime createdAt,
        boolean hasNutritionProfile,
        RegistrationSource registrationSource
) {
}
