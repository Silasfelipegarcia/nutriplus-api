package br.com.nutriplus.service;

import br.com.nutriplus.application.auth.LoginAccessPolicy;
import br.com.nutriplus.application.auth.LoginUseCase;
import br.com.nutriplus.application.auth.RefreshTokenUseCase;
import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.RegistrationSource;
import br.com.nutriplus.dto.request.RegisterRequest;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceBetaRequestTest {

    @Mock private UserRepository userRepository;
    @Mock private NutritionProfileRepository nutritionProfileRepository;
    @Mock private PasswordHasherPort passwordHasherPort;
    @Mock private TokenPort tokenPort;
    @Mock private UserQueryPort userQueryPort;
    @Mock private LoginUseCase loginUseCase;
    @Mock private RefreshTokenUseCase refreshTokenUseCase;
    @Mock private ResponseMapper responseMapper;
    @Mock private AuditLogService auditLogService;
    @Mock private CpfRegistrationService cpfRegistrationService;
    @Mock private UserRegistrationValidator userRegistrationValidator;
    @Mock private FeatureFlagService featureFlagService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                nutritionProfileRepository,
                passwordHasherPort,
                tokenPort,
                userQueryPort,
                loginUseCase,
                refreshTokenUseCase,
                responseMapper,
                auditLogService,
                cpfRegistrationService,
                userRegistrationValidator,
                featureFlagService);
    }

    @Test
    void betaRequestCreatesPendingUserWithBetaSource() {
        when(passwordHasherPort.encode("secret")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        var response = authService.betaRequest(new RegisterRequest(
                "Test", "t@test.com", "secret", "52998224725", LocalDate.of(1990, 1, 1),
                "11987654321",
                null, null, null, null));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRegistrationSource()).isEqualTo(RegistrationSource.BETA_WAITLIST);
        assertThat(captor.getValue().isLoginEnabled()).isFalse();
        assertThat(response.message()).isEqualTo(LoginAccessPolicy.BETA_WAITLIST_MESSAGE);
    }
}
