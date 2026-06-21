package br.com.nutriplus.domain.util;

import br.com.nutriplus.domain.enums.LifeStage;

import java.time.LocalDate;
import java.time.Period;

public final class LifeStageUtil {

    private LifeStageUtil() {
    }

    public static LifeStage resolve(int age) {
        if (age >= 65) {
            return LifeStage.SENIOR;
        }
        if (age >= 60) {
            return LifeStage.PRE_SENIOR;
        }
        return LifeStage.ADULT;
    }

    public static LifeStage resolveFromBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            return LifeStage.ADULT;
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return resolve(age);
    }
}
