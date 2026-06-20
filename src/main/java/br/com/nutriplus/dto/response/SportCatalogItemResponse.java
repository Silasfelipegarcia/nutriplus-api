package br.com.nutriplus.dto.response;

import java.math.BigDecimal;

public record SportCatalogItemResponse(
        String sportType,
        String label,
        double met,
        String intensityHint
) {
}
