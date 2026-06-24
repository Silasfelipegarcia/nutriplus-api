package br.com.nutriplus.domain.model;

import br.com.nutriplus.domain.enums.UserRole;

import java.time.LocalDateTime;

public record User(
        Long id,
        String name,
        String email,
        UserRole role,
        String passwordHash,
        String photoUrl,
        String photoThumbnailUrl,
        String cpfEncrypted,
        int failedLoginAttempts,
        boolean passwordMustChange,
        LocalDateTime termsAcceptedAt,
        String termsVersion,
        LocalDateTime privacyPolicyAcceptedAt,
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
