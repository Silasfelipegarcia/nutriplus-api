package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.response.AdminEmailTestResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.infrastructure.config.EmailProperties;
import br.com.nutriplus.security.AuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class AdminEmailService {

    private final AuthorizationService authorizationService;
    private final EmailSender emailSender;
    private final EmailProperties emailProperties;

    public AdminEmailService(AuthorizationService authorizationService,
                             EmailSender emailSender,
                             EmailProperties emailProperties) {
        this.authorizationService = authorizationService;
        this.emailSender = emailSender;
        this.emailProperties = emailProperties;
    }

    public AdminEmailTestResponse sendTestEmail() {
        requireAdmin();
        User admin = authorizationService.requireAuthenticated();

        if (!emailProperties.isEnabled()) {
            return new AdminEmailTestResponse(
                    false,
                    admin.getEmail(),
                    emailProperties.isResendConfigured(),
                    "E-mail desabilitado (EMAIL_ENABLED=false).");
        }

        emailSender.sendTestEmail(admin.getEmail(), admin.getName());

        String message = emailProperties.isResendConfigured()
                ? "E-mail de teste enviado via Resend."
                : "E-mail registrado apenas em log (RESEND_API_KEY ausente).";

        return new AdminEmailTestResponse(
                emailProperties.isResendConfigured(),
                admin.getEmail(),
                emailProperties.isResendConfigured(),
                message);
    }

    private void requireAdmin() {
        if (!authorizationService.hasRole(UserRole.ADMIN)) {
            throw new BusinessException("Acesso restrito a administradores.");
        }
    }
}
