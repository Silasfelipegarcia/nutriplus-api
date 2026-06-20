package br.com.nutriplus.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TrainingProfileRequest(
        @NotNull Boolean athleteModeEnabled,
        @Valid List<TrainingActivityRequest> activities
) {
}
