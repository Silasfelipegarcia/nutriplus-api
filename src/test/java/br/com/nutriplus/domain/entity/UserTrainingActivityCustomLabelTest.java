package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.SportType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTrainingActivityCustomLabelTest {

    @Test
    void storesCustomLabelForOtherSport() {
        User user = User.builder().name("Test").email("t@test.com").passwordHash("x").build();
        UserTrainingActivity activity = new UserTrainingActivity(
                user,
                SportType.OTHER,
                3,
                45,
                "Paddle"
        );

        assertEquals("Paddle", activity.getCustomLabel());
        assertEquals(SportType.OTHER, activity.getSportType());
    }
}
