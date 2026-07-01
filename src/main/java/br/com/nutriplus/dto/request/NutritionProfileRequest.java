package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NutritionProfileRequest(
        @NotNull @Min(18) @Max(120) Integer age,
        LocalDate birthDate,
        @Size(max = 2) String stateCode,
        @Size(max = 120) String city,
        ChewingDifficulty chewingDifficulty,
        Boolean seniorWeightLossAck,
        @NotNull Sex sex,
        @NotNull @DecimalMin("100.0") @DecimalMax("250.0") BigDecimal heightCm,
        @NotNull @DecimalMin("30.0") @DecimalMax("300.0") BigDecimal currentWeightKg,
        @NotNull @DecimalMin("30.0") @DecimalMax("300.0") BigDecimal targetWeightKg,
        @Min(4) @Max(104) Integer goalTargetWeeks,
        @NotNull Goal goal,
        @NotNull ActivityLevel activityLevel,
        @NotNull DietaryPreference dietaryPreference,
        @NotNull Restriction restriction,
        @NotNull AgentPersona agentPersona,
        @Size(max = 2000) String foodLikes,
        @Size(max = 2000) String foodDislikes,
        @Size(max = 2000) String mealNotes,
        Boolean eatsBreakfast,
        Boolean eatsLunch,
        Boolean eatsAfternoonSnack,
        Boolean eatsDinner,
        Boolean openToRoutineAdjustment,
        HungerPattern hungerPattern,
        NutritionMode nutritionMode,
        java.util.List<@Size(max = 80) String> freeExtras,
        FoodBudgetLevel foodBudgetLevel,
        CalculationMethod calculationMethod,
        @DecimalMin("5.0") @DecimalMax("60.0") BigDecimal bodyFatPercent,
        @DecimalMin("800.0") @DecimalMax("5000.0") BigDecimal manualBmrKcal,
        @DecimalMin("20.0") @DecimalMax("200.0") BigDecimal muscleMassKg,
        @Pattern(regexp = "^\\d{1,2}:\\d{1,2}$") String wakeTime,
        @Pattern(regexp = "^\\d{1,2}:\\d{1,2}$") String sleepTime,
        @Size(max = 2000) String healthConditions,
        @Size(max = 2000) String medications,
        @Size(max = 2000) String allergies,
        @Size(max = 2000) String healthNotes,
        PregnancyStatus pregnancyStatus,
        Boolean eatingDisorderRisk,
        Boolean severeRenalRestriction
) {
    public CalculationMethod resolvedCalculationMethod() {
        return calculationMethod != null ? calculationMethod : CalculationMethod.ESTIMATE;
    }

    public FoodBudgetLevel resolvedFoodBudgetLevel() {
        return foodBudgetLevel != null ? foodBudgetLevel : FoodBudgetLevel.MODERATE;
    }
}
