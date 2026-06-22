package br.com.nutriplus.service;

import br.com.nutriplus.domain.enums.CalculationMethod;
import br.com.nutriplus.dto.request.ProBodyMeasurementRequest;
import br.com.nutriplus.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProMeasurementValidatorTest {

    private final ProMeasurementValidator validator = new ProMeasurementValidator();

    @Test
    void estimateAllowsMissingBodyFat() {
        var request = new ProBodyMeasurementRequest(
                CalculationMethod.ESTIMATE,
                LocalDate.now(),
                BigDecimal.valueOf(72.5),
                null,
                null,
                BigDecimal.valueOf(82),
                BigDecimal.valueOf(98),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void bioimpedanceRequiresBodyFat() {
        var request = new ProBodyMeasurementRequest(
                CalculationMethod.BIOIMPEDANCE,
                LocalDate.now(),
                BigDecimal.valueOf(72.5),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertThrows(BusinessException.class, () -> validator.validate(request));
    }

    @Test
    void bioimpedanceAcceptsBodyFat() {
        var request = new ProBodyMeasurementRequest(
                CalculationMethod.BIOIMPEDANCE,
                LocalDate.now(),
                BigDecimal.valueOf(72.5),
                BigDecimal.valueOf(22.4),
                BigDecimal.valueOf(31.2),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertDoesNotThrow(() -> validator.validate(request));
    }
}
