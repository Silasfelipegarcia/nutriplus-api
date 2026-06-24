package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.AgentPersona;
import br.com.nutriplus.domain.enums.DietaryPreference;
import br.com.nutriplus.domain.enums.Goal;
import br.com.nutriplus.domain.enums.Restriction;
import jakarta.validation.constraints.Size;

public record ProPatientNutritionUpdateRequest(
        Goal goal,
        DietaryPreference dietaryPreference,
        Restriction restriction,
        AgentPersona agentPersona,
        @Size(max = 2000) String mealNotes,
        @Size(max = 2000) String healthNotes
) {
}
