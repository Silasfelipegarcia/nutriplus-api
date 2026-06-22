package br.com.nutriplus.service;

import br.com.nutriplus.domain.enums.CalculationMethod;
import br.com.nutriplus.dto.request.ProBodyMeasurementRequest;
import br.com.nutriplus.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class ProMeasurementValidator {

    public void validate(ProBodyMeasurementRequest request) {
        if (request.calculationMethod() == CalculationMethod.BIOIMPEDANCE
                && request.bodyFatPercent() == null) {
            throw new BusinessException(
                    "Modo bioimpedância exige percentual de gordura corporal.");
        }
    }
}
