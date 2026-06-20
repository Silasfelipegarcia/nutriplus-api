package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrainingActivityRequest(
        @NotNull String sportType,
        @NotNull @Min(1) @Max(7) Integer daysPerWeek,
        @NotNull @Min(5) @Max(300) Integer minutesPerSession,
        @Size(max = 80) String customLabel
) {
}
