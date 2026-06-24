package br.com.nutriplus.dto.response;

import java.util.List;

public record NutritionistPublicResponse(
        Long id,
        String name,
        String crn,
        boolean crnVerified,
        String bio,
        String specialties,
        int consultationPriceCents,
        int careDurationDays,
        String photoThumbnailUrl,
        List<String> serviceModes,
        String city,
        String stateCode,
        String locationLabel,
        String whatsappPhone,
        double averageRating,
        long ratingCount
) {
}
