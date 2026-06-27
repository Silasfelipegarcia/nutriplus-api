package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;

public record RegisterResponse(
        Long id,
        String name,
        String email,
        boolean loginEnabled,
        String message
) {
}
