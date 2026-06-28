package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt,
        boolean hasNutritionProfile,
        String photoThumbnailUrl,
        String cpfMasked,
        LocalDateTime termsAcceptedAt,
        String termsVersion,
        LocalDateTime privacyPolicyAcceptedAt,
        String privacyPolicyVersion,
        LocalDateTime healthEligibilityAcceptedAt,
        String healthEligibilityVersion
) {
}
