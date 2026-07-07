package br.com.nutriplus.service;

import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.infrastructure.config.EmailProperties;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailService implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

    private final EmailProperties emailProperties;
    private final Resend resend;

    public ResendEmailService(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
        this.resend = emailProperties.isResendConfigured()
                ? new Resend(emailProperties.getResendApiKey())
                : null;
    }

    @Override
    public void sendPasswordReset(String email, String name, String resetLink) {
        String subject = "Redefinição de senha — Nutri+";
        String html = buildPasswordResetHtml(name, resetLink);
        String text = buildPasswordResetText(name, resetLink);
        dispatch(email, subject, html, text, "reset");
    }

    @Override
    public void sendBetaAccessApproved(String email, String name, String loginLink, UserRole role) {
        String subject = "Seu acesso ao Nutri+ foi liberado";
        String html = buildBetaApprovedHtml(name, loginLink, role);
        String text = buildBetaApprovedText(name, loginLink, role);
        dispatch(email, subject, html, text, "beta-access-approved");
    }

    @Override
    public void sendBetaAccessRejected(String email, String name, String reason, UserRole role) {
        String subject = "Sobre sua solicitação de acesso ao Nutri+";
        String html = buildBetaRejectedHtml(name, reason, role);
        String text = buildBetaRejectedText(name, reason, role);
        dispatch(email, subject, html, text, "beta-access-rejected");
    }

    @Override
    public void sendNutritionistVerificationRejected(String email, String name, String reason) {
        String subject = "Sobre sua verificação no Nutri+ Pro";
        String html = buildNutritionistRejectedHtml(name, reason);
        String text = buildNutritionistRejectedText(name, reason);
        dispatch(email, subject, html, text, "nutritionist-verification-rejected");
    }

    @Override
    public void sendTestEmail(String email, String name) {
        String subject = "Teste de e-mail — Nutri+";
        String html = buildTestEmailHtml(name);
        String text = buildTestEmailText(name);
        dispatch(email, subject, html, text, "test");
    }

    @Override
    public void sendCareExpiredRatingPrompt(String email, String patientName, String nutritionistName) {
        String subject = "Avalie seu nutricionista — Nutri+";
        String html = buildCareExpiredHtml(patientName, nutritionistName);
        String text = buildCareExpiredText(patientName, nutritionistName);
        dispatch(email, subject, html, text, "care-expired-rating");
    }

    @Override
    public void sendHouseholdPlanInvitation(String email,
                                            String inviteeName,
                                            String inviterName,
                                            String inviteUrl,
                                            int expiryDays) {
        String subject = inviterName + " convidou você para o plano familiar — Nutri+";
        String greeting = greetingHtml(inviteeName);
        String html = """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <body style="font-family: system-ui, sans-serif; color: #333; line-height: 1.5;">
                <p>%s</p>
                <p><strong>%s</strong> convidou você para seguir o mesmo cardápio da casa no Nutri+ — mesmos alimentos, porções ajustadas ao seu perfil.</p>
                <p><a href="%s" style="display:inline-block;padding:12px 20px;background:#2d6a4f;color:#fff;text-decoration:none;border-radius:8px;">Aceitar convite</a></p>
                <p style="font-size:14px;color:#666;">O link expira em %d dias. Baixe o app Nutri+ se ainda não tiver instalado.</p>
                </body>
                </html>
                """.formatted(greeting, escapeHtml(inviterName), escapeHtml(inviteUrl), expiryDays);
        String text = greetingText(inviteeName) + "\n\n"
                + inviterName + " convidou você para o plano familiar no Nutri+.\n"
                + "Aceite em: " + inviteUrl + "\n"
                + "O link expira em " + expiryDays + " dias.\n";
        dispatch(email, subject, html, text, "household-plan-invite");
    }

    private String buildCareExpiredHtml(String patientName, String nutritionistName) {
        String greeting = greetingHtml(patientName);
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <body style="font-family: Arial, sans-serif; line-height: 1.5; color: #1f2937;">
                  <p>%s</p>
                  <p>Seu período de acompanhamento com <strong>%s</strong> foi encerrado.</p>
                  <p>Que tal avaliar a experiência? Sua opinião ajuda outros pacientes e o profissional.</p>
                  <p>Abra o app Nutri+ e acesse a área do nutricionista para deixar sua avaliação.</p>
                  <p>— Equipe Nutri+</p>
                </body>
                </html>
                """.formatted(greeting, escapeHtml(nutritionistName));
    }

    private String buildCareExpiredText(String patientName, String nutritionistName) {
        String greeting = greetingText(patientName);
        return """
                %s

                Seu período de acompanhamento com %s foi encerrado.

                Que tal avaliar a experiência? Abra o app Nutri+ e acesse a área do nutricionista para deixar sua avaliação.

                — Equipe Nutri+
                """.formatted(greeting, nutritionistName);
    }

    private void dispatch(String email, String subject, String html, String text, String kind) {
        if (!emailProperties.isEnabled() || resend == null) {
            log.info("E-mail {} (dev/log): destinatario={} assunto={}", kind, email, subject);
            if (log.isInfoEnabled()) {
                log.info("Corpo texto:\n{}", text);
            }
            return;
        }

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(emailProperties.formatFromAddress())
                .to(email)
                .subject(subject)
                .html(html)
                .text(text)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            log.info("E-mail {} enviado para {} (id={})", kind, email, response.getId());
        } catch (ResendException e) {
            log.error("Falha ao enviar e-mail {} para {}: {}", kind, email, e.getMessage());
            throw new IllegalStateException("Não foi possível enviar o e-mail. Tente novamente mais tarde.");
        }
    }

    private String buildPasswordResetHtml(String name, String resetLink) {
        String greeting = greetingHtml(name);
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <body style="font-family: Arial, sans-serif; line-height: 1.5; color: #1f2937;">
                  <p>%s</p>
                  <p>Recebemos uma solicitação para redefinir a senha da sua conta no Nutri+.</p>
                  <p><a href="%s" style="display:inline-block;padding:12px 20px;background:#3d8b5f;color:#fff;text-decoration:none;border-radius:6px;">Redefinir senha</a></p>
                  <p>Ou copie e cole este link no navegador:<br><a href="%s">%s</a></p>
                  <p>Este link expira em 1 hora e só pode ser usado uma vez.</p>
                  <p>Se você não solicitou a redefinição, ignore este e-mail.</p>
                  <p>— Equipe Nutri+</p>
                </body>
                </html>
                """.formatted(greeting, resetLink, resetLink, resetLink);
    }

    private String buildPasswordResetText(String name, String resetLink) {
        String greeting = greetingText(name);
        return """
                %s

                Recebemos uma solicitação para redefinir a senha da sua conta no Nutri+.

                Acesse o link abaixo para definir uma nova senha:
                %s

                Este link expira em 1 hora e só pode ser usado uma vez.

                Se você não solicitou a redefinição, ignore este e-mail.

                — Equipe Nutri+
                """.formatted(greeting, resetLink);
    }

    private String buildBetaApprovedHtml(String name, String loginLink, UserRole role) {
        String greeting = greetingHtml(name);
        String intro = role == UserRole.NUTRITIONIST
                ? "Sua solicitação ao beta do Nutri+ Pro foi aprovada. Você já pode acessar o portal do nutricionista."
                : "Sua solicitação ao beta do Nutri+ foi aprovada. Você já pode entrar e começar sua jornada alimentar.";
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <body style="font-family: Arial, sans-serif; line-height: 1.5; color: #1f2937;">
                  <p>%s</p>
                  <p>%s</p>
                  <p><a href="%s" style="display:inline-block;padding:12px 20px;background:#3d8b5f;color:#fff;text-decoration:none;border-radius:6px;">Entrar no Nutri+</a></p>
                  <p>Ou copie e cole este link no navegador:<br><a href="%s">%s</a></p>
                  <p>Se você não solicitou acesso, ignore este e-mail.</p>
                  <p>— Equipe Nutri+</p>
                </body>
                </html>
                """.formatted(greeting, intro, loginLink, loginLink, loginLink);
    }

    private String buildTestEmailHtml(String name) {
        String greeting = greetingHtml(name);
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <body style="font-family: Arial, sans-serif; line-height: 1.5; color: #1f2937;">
                  <p>%s</p>
                  <p>Este é um e-mail de teste do Nutri+. Se você recebeu esta mensagem, o Resend está configurado corretamente.</p>
                  <p>— Equipe Nutri+</p>
                </body>
                </html>
                """.formatted(greeting);
    }

    private String buildTestEmailText(String name) {
        String greeting = greetingText(name);
        return """
                %s

                Este é um e-mail de teste do Nutri+. Se você recebeu esta mensagem, o Resend está configurado corretamente.

                — Equipe Nutri+
                """.formatted(greeting);
    }

    private String buildBetaApprovedText(String name, String loginLink, UserRole role) {
        String greeting = greetingText(name);
        String intro = role == UserRole.NUTRITIONIST
                ? "Sua solicitação ao beta do Nutri+ Pro foi aprovada. Você já pode acessar o portal do nutricionista."
                : "Sua solicitação ao beta do Nutri+ foi aprovada. Você já pode entrar e começar sua jornada alimentar.";
        return """
                %s

                %s

                Acesse: %s

                Se você não solicitou acesso, ignore este e-mail.

                — Equipe Nutri+
                """.formatted(greeting, intro, loginLink);
    }

    private String buildBetaRejectedHtml(String name, String reason, UserRole role) {
        String greeting = greetingHtml(name);
        String intro = role == UserRole.NUTRITIONIST
                ? "Agradecemos o interesse no Nutri+ Pro. Neste momento não foi possível aprovar sua solicitação de acesso."
                : "Agradecemos o interesse no Nutri+. Neste momento não foi possível aprovar sua solicitação de acesso ao beta.";
        String reasonBlock = formatReasonHtml(reason);
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <body style="font-family: Arial, sans-serif; line-height: 1.5; color: #1f2937;">
                  <p>%s</p>
                  <p>%s</p>
                  %s
                  <p>Você pode tentar novamente no futuro se as condições do programa mudarem.</p>
                  <p>— Equipe Nutri+</p>
                </body>
                </html>
                """.formatted(greeting, intro, reasonBlock);
    }

    private String buildBetaRejectedText(String name, String reason, UserRole role) {
        String greeting = greetingText(name);
        String intro = role == UserRole.NUTRITIONIST
                ? "Agradecemos o interesse no Nutri+ Pro. Neste momento não foi possível aprovar sua solicitação de acesso."
                : "Agradecemos o interesse no Nutri+. Neste momento não foi possível aprovar sua solicitação de acesso ao beta.";
        String reasonBlock = formatReasonText(reason);
        return """
                %s

                %s
                %s

                Você pode tentar novamente no futuro se as condições do programa mudarem.

                — Equipe Nutri+
                """.formatted(greeting, intro, reasonBlock);
    }

    private String buildNutritionistRejectedHtml(String name, String reason) {
        String greeting = greetingHtml(name);
        String reasonBlock = formatReasonHtml(reason);
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <body style="font-family: Arial, sans-serif; line-height: 1.5; color: #1f2937;">
                  <p>%s</p>
                  <p>Após análise, não foi possível concluir a verificação do seu CRN para o marketplace do Nutri+ Pro.</p>
                  %s
                  <p>Se acredita que houve um engano, responda a este e-mail com seus dados atualizados.</p>
                  <p>— Equipe Nutri+</p>
                </body>
                </html>
                """.formatted(greeting, reasonBlock);
    }

    private String buildNutritionistRejectedText(String name, String reason) {
        String greeting = greetingText(name);
        String reasonBlock = formatReasonText(reason);
        return """
                %s

                Após análise, não foi possível concluir a verificação do seu CRN para o marketplace do Nutri+ Pro.
                %s

                Se acredita que houve um engano, responda a este e-mail com seus dados atualizados.

                — Equipe Nutri+
                """.formatted(greeting, reasonBlock);
    }

    private String formatReasonHtml(String reason) {
        if (reason == null || reason.isBlank()) {
            return "";
        }
        return "<p><strong>Observação da equipe:</strong> " + escapeHtml(reason.trim()) + "</p>";
    }

    private String formatReasonText(String reason) {
        if (reason == null || reason.isBlank()) {
            return "";
        }
        return "\nObservação da equipe: " + reason.trim() + "\n";
    }

    private String greetingHtml(String name) {
        return name != null && !name.isBlank()
                ? "Olá, " + escapeHtml(name.trim()) + "."
                : "Olá.";
    }

    private String greetingText(String name) {
        return name != null && !name.isBlank()
                ? "Olá, " + name.trim() + "."
                : "Olá.";
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
