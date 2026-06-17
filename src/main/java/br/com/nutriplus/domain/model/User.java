package br.com.nutriplus.domain.model;

import java.time.LocalDateTime;

public record User(
        Long id,
        String name,
        String email,
        String passwordHash,
        String photoUrl,
        String photoThumbnailUrl,
        int failedLoginAttempts,
        boolean passwordMustChange,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public String photoForClient() {
        if (photoThumbnailUrl != null && !photoThumbnailUrl.isBlank()) {
            return photoThumbnailUrl;
        }
        return null;
    }
}
