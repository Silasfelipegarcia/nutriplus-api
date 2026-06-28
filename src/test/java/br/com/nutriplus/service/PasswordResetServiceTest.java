package br.com.nutriplus.service;

import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.request.ResetPasswordRequest;
import br.com.nutriplus.dto.response.ForgotPasswordResponse;
import br.com.nutriplus.infrastructure.config.EmailProperties;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.util.PasswordResetTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasherPort passwordHasherPort;
    @Mock private EmailSender emailSender;
    @Mock private AuditLogService auditLogService;

    private EmailProperties emailProperties;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        emailProperties = new EmailProperties();
        emailProperties.setFrontendUrl("http://localhost:4200");
        emailProperties.setResetTokenTtlHours(1);
        service = new PasswordResetService(
                userRepository, passwordHasherPort, emailSender, emailProperties, auditLogService);
    }

    @Test
    void requestResetSendsEmailForEnabledUser() {
        User user = user("user@example.com", true);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ForgotPasswordResponse response = service.requestReset("User@Example.com");

        assertThat(response.message()).isNotBlank();
        assertThat(user.getPasswordResetTokenHash()).isNotNull();
        assertThat(user.getPasswordResetExpiresAt()).isAfter(Instant.now());

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendPasswordReset(eq("user@example.com"), eq("Usuario Teste"), linkCaptor.capture());
        assertThat(linkCaptor.getValue()).startsWith("http://localhost:4200/auth/redefinir-senha?token=");
        verify(auditLogService).log("PASSWORD_RESET_REQUEST", "USER", user);
    }

    @Test
    void requestResetUnknownEmailReturnsGenericMessageWithoutSending() {
        when(userRepository.findByEmailIgnoreCase("nao@existe.com")).thenReturn(Optional.empty());

        ForgotPasswordResponse response = service.requestReset("nao@existe.com");

        assertThat(response.message()).isNotBlank();
        verifyNoInteractions(emailSender);
        verify(userRepository, never()).save(any());
    }

    @Test
    void requestResetPendingUserDoesNotSendEmail() {
        User user = user("pending@example.com", false);
        when(userRepository.findByEmailIgnoreCase("pending@example.com")).thenReturn(Optional.of(user));

        ForgotPasswordResponse response = service.requestReset("pending@example.com");

        assertThat(response.message()).isNotBlank();
        verifyNoInteractions(emailSender);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPasswordUpdatesHashAndClearsToken() {
        String token = PasswordResetTokenUtil.generateToken();
        User user = user("user@example.com", true);
        user.setPasswordResetTokenHash(PasswordResetTokenUtil.hashToken(token));
        user.setPasswordResetExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        when(userRepository.findByPasswordResetTokenHash(PasswordResetTokenUtil.hashToken(token)))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordHasherPort.encode("nova123")).thenReturn("hash-nova");

        service.resetPassword(new ResetPasswordRequest(token, "nova123"));

        assertThat(user.getPasswordHash()).isEqualTo("hash-nova");
        assertThat(user.getPasswordResetTokenHash()).isNull();
        assertThat(user.getFailedLoginAttempts()).isZero();
        verify(auditLogService).log("PASSWORD_RESET_COMPLETE", "USER", user);
    }

    @Test
    void resetPasswordInvalidTokenFails() {
        ResetPasswordRequest request = new ResetPasswordRequest(PasswordResetTokenUtil.generateToken(), "nova123");
        when(userRepository.findByPasswordResetTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
    }

    private static User user(String email, boolean loginEnabled) {
        return User.builder()
                .id(1L)
                .name("Usuario Teste")
                .email(email)
                .role(UserRole.PATIENT)
                .loginEnabled(loginEnabled)
                .passwordHash("old-hash")
                .build();
    }
}
