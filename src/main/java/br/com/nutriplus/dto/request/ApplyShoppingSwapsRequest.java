package br.com.nutriplus.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ApplyShoppingSwapsRequest(
        @NotEmpty @Valid List<ShoppingSwapSelectionRequest> selections
) {
}
