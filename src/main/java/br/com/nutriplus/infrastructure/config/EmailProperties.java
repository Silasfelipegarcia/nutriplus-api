package br.com.nutriplus.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nutriplus.email")
public class EmailProperties {

    private boolean enabled = true;
    private String resendApiKey = "";
    private String from = "noreply@nutriplus.app.br";
    private String fromName = "Nutri+";
    private String frontendUrl = "http://localhost:4200";
    private int resetTokenTtlHours = 1;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getResendApiKey() {
        return resendApiKey;
    }

    public void setResendApiKey(String resendApiKey) {
        this.resendApiKey = resendApiKey;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public int getResetTokenTtlHours() {
        return resetTokenTtlHours;
    }

    public void setResetTokenTtlHours(int resetTokenTtlHours) {
        this.resetTokenTtlHours = resetTokenTtlHours;
    }

    public boolean isResendConfigured() {
        return resendApiKey != null && !resendApiKey.isBlank();
    }

    public String formatFromAddress() {
        if (fromName == null || fromName.isBlank()) {
            return from;
        }
        return fromName.trim() + " <" + from.trim() + ">";
    }
}
