package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;

public record CareRatingResponse(
        Long id,
        Long careRelationshipId,
        int stars,
        String comment,
        String patientName,
        LocalDateTime createdAt
) {
}
