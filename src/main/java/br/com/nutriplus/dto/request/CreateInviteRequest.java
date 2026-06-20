package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CreateInviteRequest(
        @Min(1) @Max(1000) Integer maxUses,
        Integer expiresInDays
) {
}
