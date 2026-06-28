package br.com.nutriplus.service;

import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.dto.request.ResetPasswordRequest;
import br.com.nutriplus.dto.response.ForgotPasswordResponse;
import br.com.nutriplus.infrastructure.config.EmailProperties;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.util.PasswordResetTokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final String GENERIC_MESSAGE =
            "Se o e-mail estiver cadastrado, você receberá instruções para redefinir sua senha em instantes.";

    private final UserRepository userRepository;
    private final PasswordHasherPort passwordHasherPort;
    private final EmailSender emailSender;
    private final EmailProperties emailProperties;
    private final AuditLogService auditLogService;

    public PasswordResetService(UserRepository userRepository,
                                  PasswordHasherPort passwordHasherPort,
                                  EmailSender emailSender,
                                  EmailProperties emailProperties,
                                  AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordHasherPort = passwordHasherPort;
        this.emailSender = emailSender;
        this.emailProperties = emailProperties;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ForgotPasswordResponse requestReset(String email) {
        String normalizedEmail = normalizeEmail(email);
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(normalizedEmail);

        if (userOpt.isEmpty()) {
            return new ForgotPasswordResponse(GENERIC_MESSAGE);
        }

        User user = userOpt.get();
        if (!user.isLoginEnabled()) {
            return new ForgotPasswordResponse(GENERIC_MESSAGE);
        }

        String token = PasswordResetTokenUtil.generateToken();
        user.setPasswordResetTokenHash(PasswordResetTokenUtil.hashToken(token));
        user.setPasswordResetExpiresAt(Instant.now().plus(
                emailProperties.getResetTokenTtlHours(), ChronoUnit.HOURS));
        userRepository.save(user);

        String resetLink = buildResetLink(token);
        emailSender.sendPasswordReset(user.getEmail(), user.getName(), resetLink);
        auditLogService.log("PASSWORD_RESET_REQUEST", "USER", user);

        return new ForgotPasswordResponse(GENERIC_MESSAGE);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request.newPassword() == null || request.newPassword().length() < 6) {
            throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres");
        }

        String tokenHash = PasswordResetTokenUtil.hashToken(request.token().trim());
        User user = userRepository.findByPasswordResetTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Link de redefinição inválido ou expirado"));

        if (!user.isPasswordResetTokenValid()) {
            clearResetToken(user);
            userRepository.save(user);
            throw new IllegalArgumentException("Link de redefinição inválido ou expirado");
        }

        user.setPasswordHash(passwordHasherPort.encode(request.newPassword()));
        user.setFailedLoginAttempts(0);
        user.setPasswordMustChange(false);
        clearResetToken(user);
        userRepository.save(user);
        auditLogService.log("PASSWORD_RESET_COMPLETE", "USER", user);
    }

    private void clearResetToken(User user) {
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetExpiresAt(null);
    }

    private String buildResetLink(String token) {
        String base = emailProperties.getFrontendUrl().replaceAll("/$", "");
        return base + "/auth/redefinir-senha?token=" + token;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
