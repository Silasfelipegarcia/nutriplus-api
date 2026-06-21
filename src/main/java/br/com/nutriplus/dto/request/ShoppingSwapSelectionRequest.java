package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShoppingSwapSelectionRequest(
        @NotNull Long shoppingListItemId,
        @NotBlank String swapOptionId
) {
}
