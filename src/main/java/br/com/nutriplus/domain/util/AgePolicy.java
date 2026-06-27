package br.com.nutriplus.domain.util;

import br.com.nutriplus.exception.BusinessException;

import java.time.LocalDate;
import java.time.Period;

public final class AgePolicy {

    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 120;

    private AgePolicy() {
    }

    public static int fromBirthDate(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public static void requireAdult(LocalDate birthDate) {
        int age = fromBirthDate(birthDate);
        if (age < MIN_AGE) {
            throw new BusinessException("Você precisa ter pelo menos 18 anos.");
        }
        if (age > MAX_AGE) {
            throw new BusinessException("Informe uma data de nascimento válida.");
        }
    }
}
