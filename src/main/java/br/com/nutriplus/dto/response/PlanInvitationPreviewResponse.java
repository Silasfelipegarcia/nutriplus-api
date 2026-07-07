package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;

public record PlanInvitationPreviewResponse(
        String token,
        String inviterName,
        String inviteeName,
        boolean expired,
        boolean requiresRegistration,
        LocalDateTime expiresAt
) {
}
