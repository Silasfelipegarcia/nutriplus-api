package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record ProductEventItemRequest(
        @NotBlank @Size(max = 128) String eventName,
        @Size(max = 128) String step,
        Map<String, Object> properties
) {
}
