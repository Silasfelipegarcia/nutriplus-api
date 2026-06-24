package br.com.nutriplus.application.auth;

import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private UserQueryPort userQueryPort;

    @Mock
    private TokenPort tokenPort;

    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;

    private static final User USER = new User(
            1L, "Test", "test@nutriplus.com", UserRole.PATIENT, "hash",
            null, null, null, 0, false, null, null, null, LocalDateTime.now(), LocalDateTime.now());

    @Test
    void refreshReturnsNewTokens() {
        when(tokenPort.extractRefreshUserId("refresh-token")).thenReturn(1L);
        when(userQueryPort.findById(1L)).thenReturn(Optional.of(USER));
        when(tokenPort.generateAccessToken(USER)).thenReturn("access");
        when(tokenPort.generateRefreshToken(USER)).thenReturn("refresh");
        when(tokenPort.accessExpirationSeconds()).thenReturn(3600L);

        LoginUseCase.Response response = refreshTokenUseCase.execute("refresh-token");

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void refreshFailsWhenUserMissing() {
        when(tokenPort.extractRefreshUserId("refresh-token")).thenReturn(99L);
        when(userQueryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenUseCase.execute("refresh-token"))
                .isInstanceOf(TokenPort.InvalidTokenException.class);
    }
}
