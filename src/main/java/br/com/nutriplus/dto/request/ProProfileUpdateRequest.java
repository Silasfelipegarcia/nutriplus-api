package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.ServiceMode;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProProfileUpdateRequest(
        @Size(max = 2000) String bio,
        @Size(max = 500) String specialties,
        Boolean marketplaceVisible,
        List<ServiceMode> serviceModes,
        @Size(max = 120) String city,
        @Size(min = 2, max = 2) String stateCode,
        @Size(max = 120) String neighborhood,
        @Size(max = 20) String whatsappPhone,
        @Size(max = 2000) String formation,
        Integer experienceYears,
        @Size(max = 500) String approach,
        List<@Size(min = 2, max = 5) String> languages
) {
}
