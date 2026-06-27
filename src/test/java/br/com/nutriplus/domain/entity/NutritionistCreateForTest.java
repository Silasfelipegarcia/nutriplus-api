package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NutritionistCreateForTest {

    @Test
    void createForStartsWithCrnUnverified() {
        User user = User.builder().name("N").email("n@test.com").role(UserRole.NUTRITIONIST).build();
        Nutritionist nutritionist = Nutritionist.createFor(user, "CRN-3 1", "bio", "esp", 7900, 30);

        assertThat(nutritionist.isCrnVerified()).isFalse();
    }
}
