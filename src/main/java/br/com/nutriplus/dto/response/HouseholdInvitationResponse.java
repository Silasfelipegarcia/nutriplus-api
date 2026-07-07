package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;

public record HouseholdInvitationResponse(
        Long id,
        String inviteeEmail,
        String inviteeName,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
