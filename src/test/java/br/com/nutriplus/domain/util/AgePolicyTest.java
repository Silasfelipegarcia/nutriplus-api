package br.com.nutriplus.domain.util;

import br.com.nutriplus.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgePolicyTest {

    @Test
    void acceptsAdultBirthDate() {
        AgePolicy.requireAdult(LocalDate.now().minusYears(25));
    }

    @Test
    void rejectsMinor() {
        assertThatThrownBy(() -> AgePolicy.requireAdult(LocalDate.now().minusYears(17)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("18 anos");
    }

    @Test
    void computesAgeFromBirthDate() {
        int age = AgePolicy.fromBirthDate(LocalDate.of(1990, 6, 15));
        assertThat(age).isGreaterThanOrEqualTo(35);
    }
}
