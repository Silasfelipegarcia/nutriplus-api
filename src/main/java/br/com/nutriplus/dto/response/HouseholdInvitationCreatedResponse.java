package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;

public record HouseholdInvitationCreatedResponse(
        Long invitationId,
        String inviteeEmail,
        String inviteeName,
        String inviteUrl,
        LocalDateTime expiresAt
) {
}
