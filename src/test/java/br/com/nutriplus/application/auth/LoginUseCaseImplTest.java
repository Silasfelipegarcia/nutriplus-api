package br.com.nutriplus.application.auth;

import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.application.port.UserUpdatePort;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.exception.AccountLockedException;
import br.com.nutriplus.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import br.com.nutriplus.exception.LoginDisabledException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    @Mock
    private UserQueryPort userQueryPort;

    @Mock
    private UserUpdatePort userUpdatePort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private TokenPort tokenPort;

    private LoginUseCaseImpl loginUseCase;

    private static final User ACTIVE_USER = new User(
            1L,
            "Test User",
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
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCaseImpl(userQueryPort, userUpdatePort, passwordHasherPort, tokenPort);
    }

    @Test
    void loginSuccessReturnsTokens() {
        when(userQueryPort.findByEmail("test@nutriplus.com")).thenReturn(Optional.of(ACTIVE_USER));
        when(passwordHasherPort.matches("secret123", "hash")).thenReturn(true);
        when(userQueryPort.findById(1L)).thenReturn(Optional.of(ACTIVE_USER));
        when(tokenPort.generateAccessToken(ACTIVE_USER)).thenReturn("access");
        when(tokenPort.generateRefreshToken(ACTIVE_USER)).thenReturn("refresh");
        when(tokenPort.accessExpirationSeconds()).thenReturn(3600L);

        LoginUseCase.Response response = loginUseCase.execute(
                new LoginUseCase.Request("test@nutriplus.com", "secret123"));

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        assertThat(response.user().email()).isEqualTo("test@nutriplus.com");
        verify(userUpdatePort).resetFailedLoginAttempts(1L);
    }

    @Test
    void wrongPasswordIncrementsAttempts() {
        when(userQueryPort.findByEmail("test@nutriplus.com")).thenReturn(Optional.of(ACTIVE_USER));
        when(passwordHasherPort.matches("wrong", "hash")).thenReturn(false);
        when(userUpdatePort.incrementFailedLoginAttempts(1L)).thenReturn(1);

        assertThatThrownBy(() -> loginUseCase.execute(new LoginUseCase.Request("test@nutriplus.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void lockedAccountRejected() {
        User locked = new User(
                1L, "Test", "test@nutriplus.com", UserRole.PATIENT, true, "hash",
                null, null, null, 3, false, null, null, null, LocalDateTime.now(), LocalDateTime.now());
        when(userQueryPort.findByEmail("test@nutriplus.com")).thenReturn(Optional.of(locked));

        assertThatThrownBy(() -> loginUseCase.execute(new LoginUseCase.Request("test@nutriplus.com", "secret123")))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    void loginDisabledRejected() {
        User pending = new User(
                1L, "Test", "test@nutriplus.com", UserRole.PATIENT, false, "hash",
                null, null, null, 0, false, null, null, null, LocalDateTime.now(), LocalDateTime.now());
        when(userQueryPort.findByEmail("test@nutriplus.com")).thenReturn(Optional.of(pending));
        when(passwordHasherPort.matches("secret123", "hash")).thenReturn(true);

        assertThatThrownBy(() -> loginUseCase.execute(new LoginUseCase.Request("test@nutriplus.com", "secret123")))
                .isInstanceOf(LoginDisabledException.class);
    }

    @Test
    void unknownEmailRejected() {
        when(userQueryPort.findByEmail("missing@nutriplus.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.execute(new LoginUseCase.Request("missing@nutriplus.com", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
