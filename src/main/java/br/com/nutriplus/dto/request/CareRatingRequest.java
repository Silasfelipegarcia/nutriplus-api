package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CareRatingRequest(
        @NotNull @Min(1) @Max(5) Integer stars,
        @Size(max = 2000) String comment
) {
}
