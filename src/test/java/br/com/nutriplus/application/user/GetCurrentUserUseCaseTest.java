package br.com.nutriplus.application.user;

import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.application.shared.ActingUserResolver;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.exception.ResourceNotFoundException;
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
class GetCurrentUserUseCaseTest {

    @Mock
    private ActingUserResolver actingUserResolver;

    @Mock
    private UserQueryPort userQueryPort;

    @InjectMocks
    private GetCurrentUserUseCase getCurrentUserUseCase;

    private static final User USER = new User(
            1L, "Test", "test@nutriplus.com", UserRole.PATIENT, "hash",
            null, null, null, 0, false, null, null, null, LocalDateTime.now(), LocalDateTime.now());

    @Test
    void returnsCurrentUser() {
        when(actingUserResolver.resolveUserId()).thenReturn(1L);
        when(userQueryPort.findById(1L)).thenReturn(Optional.of(USER));

        assertThat(getCurrentUserUseCase.execute()).isEqualTo(USER);
    }

    @Test
    void throwsWhenUserMissing() {
        when(actingUserResolver.resolveUserId()).thenReturn(99L);
        when(userQueryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getCurrentUserUseCase.execute())
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
