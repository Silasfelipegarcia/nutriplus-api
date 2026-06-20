package br.com.nutriplus.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductEventsBatchRequest(
        @NotBlank @Size(max = 64) String sessionId,
        @NotEmpty @Valid List<ProductEventItemRequest> events
) {
}
