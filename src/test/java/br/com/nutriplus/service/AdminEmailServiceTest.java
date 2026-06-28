package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.infrastructure.config.EmailProperties;
import br.com.nutriplus.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminEmailServiceTest {

    @Mock private AuthorizationService authorizationService;
    @Mock private EmailSender emailSender;

    private EmailProperties emailProperties;
    private AdminEmailService service;

    @BeforeEach
    void setUp() {
        emailProperties = new EmailProperties();
        emailProperties.setEnabled(true);
        emailProperties.setResendApiKey("re_test");
        service = new AdminEmailService(authorizationService, emailSender, emailProperties);
    }

    @Test
    void sendTestEmailRequiresAdmin() {
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(false);

        assertThatThrownBy(() -> service.sendTestEmail())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("administradores");
    }

    @Test
    void sendTestEmailDispatchesToCurrentAdmin() {
        User admin = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@nutriplus.app.br")
                .role(UserRole.ADMIN)
                .build();
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(authorizationService.requireAuthenticated()).thenReturn(admin);

        var response = service.sendTestEmail();

        verify(emailSender).sendTestEmail("admin@nutriplus.app.br", "Admin");
        assertThat(response.sent()).isTrue();
        assertThat(response.recipient()).isEqualTo("admin@nutriplus.app.br");
    }

    @Test
    void sendTestEmailWhenDisabledReturnsMessage() {
        emailProperties.setEnabled(false);
        User admin = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@nutriplus.app.br")
                .role(UserRole.ADMIN)
                .build();
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(authorizationService.requireAuthenticated()).thenReturn(admin);

        var response = service.sendTestEmail();

        assertThat(response.sent()).isFalse();
        assertThat(response.message()).contains("desabilitado");
    }
}
