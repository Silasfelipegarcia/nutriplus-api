package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.CareRelationshipSource;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import br.com.nutriplus.domain.enums.PreferredCareMode;

import java.time.LocalDateTime;

public record CareRelationshipResponse(
        Long id,
        Long patientId,
        String patientName,
        Long nutritionistId,
        String nutritionistName,
        CareRelationshipStatus status,
        CareRelationshipSource source,
        PreferredCareMode preferredCareMode,
        LocalDateTime startedAt,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
