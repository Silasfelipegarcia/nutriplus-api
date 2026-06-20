package br.com.nutriplus.dto.response;

public record DataConsentResponse(
        Long careRelationshipId,
        String scopes,
        java.time.LocalDateTime grantedAt,
        String consentVersion
) {
}
