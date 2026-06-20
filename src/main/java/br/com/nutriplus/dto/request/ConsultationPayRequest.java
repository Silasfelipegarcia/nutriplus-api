package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.NotNull;

public record ConsultationPayRequest(
        @NotNull Long nutritionistId
) {
}
