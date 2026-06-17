package br.com.nutriplus.application.user;

import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.application.port.UserUpdatePort;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.dto.request.UpdateUserProfileRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileUseCaseTest {

    @Mock
    private UserQueryPort userQueryPort;

    @Mock
    private UserUpdatePort userUpdatePort;

    @InjectMocks
    private UpdateUserProfileUseCase useCase;

    @Test
    void updatesNameOnly() {
        User current = sampleUser("Old Name", null, null);
        when(userQueryPort.findById(1L)).thenReturn(Optional.of(current));
        when(userUpdatePort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = useCase.execute(1L, new UpdateUserProfileRequest("New Name", null));

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.photoUrl()).isNull();
    }

    @Test
    void clearsPhotoWhenBlank() {
        User current = sampleUser("User", "data:image/jpeg;base64,abc", "data:image/jpeg;base64,thumb");
        when(userQueryPort.findById(1L)).thenReturn(Optional.of(current));
        when(userUpdatePort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = useCase.execute(1L, new UpdateUserProfileRequest(null, "  "));

        assertThat(result.photoUrl()).isNull();
        assertThat(result.photoThumbnailUrl()).isNull();
    }

    @Test
    void savesUpdatedUser() {
        User current = sampleUser("User", null, null);
        when(userQueryPort.findById(1L)).thenReturn(Optional.of(current));
        when(userUpdatePort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L, new UpdateUserProfileRequest("Updated", null));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userUpdatePort).save(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Updated");
    }

    private static User sampleUser(String name, String photoUrl, String photoThumbnailUrl) {
        return new User(
                1L,
                name,
                "user@test.com",
                "hash",
                photoUrl,
                photoThumbnailUrl,
                0,
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
