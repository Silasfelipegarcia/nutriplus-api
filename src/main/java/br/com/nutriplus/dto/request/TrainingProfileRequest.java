package br.com.nutriplus.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record TrainingProfileRequest(
        @NotNull Boolean athleteModeEnabled,
        @Valid List<TrainingActivityRequest> activities,
        @Pattern(regexp = "^\\d{1,2}:\\d{1,2}$") String primaryTrainingTime,
        @Valid AthleteHungerByMealRequest athleteHungerByMeal
) {
}
