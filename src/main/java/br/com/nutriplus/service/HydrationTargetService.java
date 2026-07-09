package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Meta diária de água (~35 ml/kg) com bônus atleta e guardrails clínicos.
 */
@Service
public class HydrationTargetService {

    static final int ML_PER_KG = 35;
    static final int ROUND_TO_ML = 50;
    static final int ATHLETE_BONUS_ML = 500;
    static final int MAX_ML = 4000;

    public void syncHydrationTarget(NutritionProfile profile) {
        profile.setDailyWaterTargetMl(computeDailyWaterTargetMl(profile));
    }

    public Integer computeDailyWaterTargetMl(NutritionProfile profile) {
        if (profile.isSevereRenalRestriction()) {
            return null;
        }
        BigDecimal weightKg = profile.getCurrentWeightKg();
        if (weightKg == null || weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        int baseMl = roundToNearest(
                weightKg.multiply(BigDecimal.valueOf(ML_PER_KG)).setScale(0, RoundingMode.HALF_UP).intValue(),
                ROUND_TO_ML);

        if (profile.isAthleteModeEnabled()
                && profile.getTrainingDailyExtraKcal() != null
                && profile.getTrainingDailyExtraKcal().compareTo(BigDecimal.ZERO) > 0) {
            baseMl += ATHLETE_BONUS_ML;
        }

        return Math.min(baseMl, MAX_ML);
    }

    static int roundToNearest(int value, int step) {
        if (step <= 0) {
            return value;
        }
        return Math.round((float) value / step) * step;
    }
}
