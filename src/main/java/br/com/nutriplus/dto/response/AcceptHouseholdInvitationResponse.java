package br.com.nutriplus.dto.response;

public record AcceptHouseholdInvitationResponse(
        Long householdId,
        boolean planGenerationStarted,
        String message
) {
}
