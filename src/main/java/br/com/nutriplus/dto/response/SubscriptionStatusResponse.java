package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.SubscriptionPlan;

import java.time.Instant;

public class SubscriptionStatusResponse {
    private String status;
    private boolean autoRenew;
    private Instant validUntil;
    private Instant cancelledAt;
    private String defaultCardId;
    private SubscriptionPlan plan;
    private String planNome;
    private long daysRemaining;
    private boolean podeCancelar;
    private boolean podeReativar;
    private boolean trialDisponivel;
    private boolean emTrial;
    private boolean billingEnforced;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isAutoRenew() { return autoRenew; }
    public void setAutoRenew(boolean autoRenew) { this.autoRenew = autoRenew; }
    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getDefaultCardId() { return defaultCardId; }
    public void setDefaultCardId(String defaultCardId) { this.defaultCardId = defaultCardId; }
    public SubscriptionPlan getPlan() { return plan; }
    public void setPlan(SubscriptionPlan plan) { this.plan = plan; }
    public String getPlanNome() { return planNome; }
    public void setPlanNome(String planNome) { this.planNome = planNome; }
    public long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(long daysRemaining) { this.daysRemaining = daysRemaining; }
    public boolean isPodeCancelar() { return podeCancelar; }
    public void setPodeCancelar(boolean podeCancelar) { this.podeCancelar = podeCancelar; }
    public boolean isPodeReativar() { return podeReativar; }
    public void setPodeReativar(boolean podeReativar) { this.podeReativar = podeReativar; }
    public boolean isTrialDisponivel() { return trialDisponivel; }
    public void setTrialDisponivel(boolean trialDisponivel) { this.trialDisponivel = trialDisponivel; }
    public boolean isEmTrial() { return emTrial; }
    public void setEmTrial(boolean emTrial) { this.emTrial = emTrial; }
    public boolean isBillingEnforced() { return billingEnforced; }
    public void setBillingEnforced(boolean billingEnforced) { this.billingEnforced = billingEnforced; }
}
