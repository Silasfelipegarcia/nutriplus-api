package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.infrastructure.config.EmailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BetaAccessNotificationService {

    private static final Logger log = LoggerFactory.getLogger(BetaAccessNotificationService.class);

    private final EmailSender emailSender;
    private final EmailProperties emailProperties;
    private final AuditLogService auditLogService;

    public BetaAccessNotificationService(EmailSender emailSender,
                                         EmailProperties emailProperties,
                                         AuditLogService auditLogService) {
        this.emailSender = emailSender;
        this.emailProperties = emailProperties;
        this.auditLogService = auditLogService;
    }

    public boolean notifyApproved(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return false;
        }
        String loginLink = buildLoginLink(user);
        try {
            emailSender.sendBetaAccessApproved(
                    user.getEmail(),
                    user.getName(),
                    loginLink,
                    user.getRole() != null ? user.getRole() : UserRole.PATIENT);
            auditLogService.log("BETA_ACCESS_EMAIL_SENT", "USER", user);
            return true;
        } catch (RuntimeException ex) {
            log.error("Falha ao enviar e-mail de aprovação beta para userId={}: {}", user.getId(), ex.getMessage());
            auditLogService.log("BETA_ACCESS_EMAIL_FAILED", "USER", user);
            return false;
        }
    }

    public boolean notifyRejected(User user, String reason) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return false;
        }
        String normalizedReason = normalizeReason(reason);
        try {
            emailSender.sendBetaAccessRejected(
                    user.getEmail(),
                    user.getName(),
                    normalizedReason,
                    user.getRole() != null ? user.getRole() : UserRole.PATIENT);
            auditLogService.log("BETA_ACCESS_REJECT_EMAIL_SENT", "USER", user);
            return true;
        } catch (RuntimeException ex) {
            log.error("Falha ao enviar e-mail de recusa beta para userId={}: {}", user.getId(), ex.getMessage());
            auditLogService.log("BETA_ACCESS_REJECT_EMAIL_FAILED", "USER", user);
            return false;
        }
    }

    public boolean notifyNutritionistVerificationRejected(User user, String reason) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return false;
        }
        String normalizedReason = normalizeReason(reason);
        try {
            emailSender.sendNutritionistVerificationRejected(
                    user.getEmail(),
                    user.getName(),
                    normalizedReason);
            auditLogService.log("NUTRITIONIST_REJECT_EMAIL_SENT", "USER", user);
            return true;
        } catch (RuntimeException ex) {
            log.error("Falha ao enviar e-mail de recusa CRN para userId={}: {}", user.getId(), ex.getMessage());
            auditLogService.log("NUTRITIONIST_REJECT_EMAIL_FAILED", "USER", user);
            return false;
        }
    }

    private static String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }
        String trimmed = reason.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildLoginLink(User user) {
        String base = emailProperties.getFrontendUrl().replaceAll("/$", "");
        if (user.getRole() == UserRole.NUTRITIONIST) {
            return base + "/auth/login";
        }
        return base + "/auth/login";
    }
}
