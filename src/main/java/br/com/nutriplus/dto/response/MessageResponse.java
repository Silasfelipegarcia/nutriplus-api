package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long senderId,
        String senderName,
        String body,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        boolean mine
) {
}
