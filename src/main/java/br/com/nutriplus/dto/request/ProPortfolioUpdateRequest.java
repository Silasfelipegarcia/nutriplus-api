package br.com.nutriplus.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProPortfolioUpdateRequest(
        @Size(max = 5) List<@Valid PortfolioItemInput> items
) {
    public record PortfolioItemInput(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 1000) String summary
    ) {
    }
}
