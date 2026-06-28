package br.com.nutriplus.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nutriplus.legal")
public record LegalProperties(
        String version,
        String privacyVersion,
        String healthEligibilityVersion,
        String termsTitle,
        String privacyTitle
) {
    public LegalProperties {
        if (version == null || version.isBlank()) {
            version = "2026-06-2";
        }
        if (privacyVersion == null || privacyVersion.isBlank()) {
            privacyVersion = version;
        }
        if (healthEligibilityVersion == null || healthEligibilityVersion.isBlank()) {
            healthEligibilityVersion = version;
        }
        if (termsTitle == null || termsTitle.isBlank()) {
            termsTitle = "Termos de Uso";
        }
        if (privacyTitle == null || privacyTitle.isBlank()) {
            privacyTitle = "Política de Privacidade";
        }
    }
}
