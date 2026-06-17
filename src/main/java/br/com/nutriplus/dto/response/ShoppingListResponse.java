package br.com.nutriplus.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ShoppingListResponse(
        Long id,
        LocalDate weekStart,
        LocalDate weekEnd,
        List<ShoppingListItemResponse> items,
        LocalDateTime createdAt
) {
}
