package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.AthleteMealHunger;

public record AthleteHungerByMealResponse(
        AthleteMealHunger breakfast,
        AthleteMealHunger lunch,
        AthleteMealHunger afternoonSnack,
        AthleteMealHunger dinner
) {
}
