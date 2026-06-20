package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;

public record InviteResponse(
        String code,
        String inviteUrl,
        Integer maxUses,
        int useCount,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
