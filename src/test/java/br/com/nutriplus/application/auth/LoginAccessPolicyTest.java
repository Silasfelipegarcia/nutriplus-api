package br.com.nutriplus.application.auth;

import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.exception.LoginDisabledException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAccessPolicyTest {

    @Test
    void adminAlwaysAllowed() {
        User admin = user(UserRole.ADMIN, true, null);
        LoginAccessPolicy.ensureCanLogin(admin);
    }

    @Test
    void rejectedUserGetsRejectedMessage() {
        User rejected = user(UserRole.PATIENT, false, LocalDateTime.now());

        assertThatThrownBy(() -> LoginAccessPolicy.ensureCanLogin(rejected))
                .isInstanceOf(LoginDisabledException.class)
                .hasMessage(LoginAccessPolicy.REJECTED_MESSAGE);
    }

    @Test
    void pendingUserGetsPendingMessage() {
        User pending = user(UserRole.PATIENT, false, null);

        assertThatThrownBy(() -> LoginAccessPolicy.ensureCanLogin(pending))
                .isInstanceOf(LoginDisabledException.class)
                .hasMessage(LoginAccessPolicy.PENDING_MESSAGE);
    }

    @Test
    void messageForRejectedUsesRejectedCodePath() {
        User rejected = user(UserRole.PATIENT, false, LocalDateTime.now());
        assertThat(LoginAccessPolicy.messageFor(rejected)).isEqualTo(LoginAccessPolicy.REJECTED_MESSAGE);
    }

    private static User user(UserRole role, boolean loginEnabled, LocalDateTime rejectedAt) {
        return new User(
                1L,
                "Test",
                "test@nutriplus.com",
                role,
                loginEnabled,
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
                LocalDateTime.now(),
                rejectedAt
        );
    }
}
