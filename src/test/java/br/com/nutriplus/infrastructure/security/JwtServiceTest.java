package br.com.nutriplus.infrastructure.security;

import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.domain.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void rejectsAccessTokenAsRefresh() {
        JwtProperties props = new JwtProperties(
                "bnV0cmlwbHVzLWRldi1zZWNyZXQta2V5LW1pbmltdW0tMzItYnl0ZXM=",
                3600,
                2592000
        );
        JwtService jwtService = new JwtService(props);
        User user = new User(
                1L,
                "Test",
                "test@nutriplus.com",
                UserRole.PATIENT,
                true,
                "hash",
                null,
                null,
                null,
                0,
                false,
                null,
                null,
                null,
                null,
                null,
                null
        );

        String access = jwtService.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.extractRefreshUserId(access))
                .isInstanceOf(br.com.nutriplus.application.port.TokenPort.InvalidTokenException.class);
    }

    @Test
    void refreshTokenRoundTrip() {
        JwtProperties props = new JwtProperties(
                "bnV0cmlwbHVzLWRldi1zZWNyZXQta2V5LW1pbmltdW0tMzItYnl0ZXM=",
                3600,
                2592000
        );
        JwtService jwtService = new JwtService(props);
        User user = new User(
                42L,
                "User",
                "user@nutriplus.com",
                UserRole.PATIENT,
                true,
                "hash",
                null,
                null,
                null,
                0,
                false,
                null,
                null,
                null,
                null,
                null,
                null
        );

        String refresh = jwtService.generateRefreshToken(user);
        assertThat(jwtService.extractRefreshUserId(refresh)).isEqualTo(42L);
    }
}
