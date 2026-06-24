package br.com.nutriplus.infrastructure.validation;

import br.com.nutriplus.domain.util.CpfUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfConstraintValidator implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return CpfUtil.isValid(value);
    }
}
