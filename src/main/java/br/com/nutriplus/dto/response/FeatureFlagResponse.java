package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;

public record FeatureFlagResponse(
        String code,
        String name,
        String description,
        boolean enabled,
        LocalDateTime updatedAt
) {
}
