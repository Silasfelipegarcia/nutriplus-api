package br.com.nutriplus.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class EmailProductionValidator implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(EmailProductionValidator.class);

    private final Environment environment;
    private final EmailProperties emailProperties;

    public EmailProductionValidator(Environment environment, EmailProperties emailProperties) {
        this.environment = environment;
        this.emailProperties = emailProperties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!isProdProfile()) {
            return;
        }
        if (emailProperties.isEnabled() && !emailProperties.isResendConfigured()) {
            throw new IllegalStateException(
                    "RESEND_API_KEY é obrigatória em produção quando EMAIL_ENABLED=true");
        }
        if (emailProperties.isEnabled()) {
            log.info(
                    "E-mail transacional ativo: from={}, frontendUrl={}",
                    emailProperties.formatFromAddress(),
                    emailProperties.getFrontendUrl());
        }
    }

    private boolean isProdProfile() {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equals);
    }
}
