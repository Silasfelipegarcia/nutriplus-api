package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Size;

public record RejectUserAccessRequest(
        @Size(max = 500) String reason
) {
}
