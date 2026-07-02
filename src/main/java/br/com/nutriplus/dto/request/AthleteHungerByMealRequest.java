package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.AthleteMealHunger;

public record AthleteHungerByMealRequest(
        AthleteMealHunger breakfast,
        AthleteMealHunger lunch,
        AthleteMealHunger afternoonSnack,
        AthleteMealHunger dinner
) {
}
