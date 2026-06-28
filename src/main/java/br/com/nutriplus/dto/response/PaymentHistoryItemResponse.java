package br.com.nutriplus.dto.response;

import java.time.Instant;

public class PaymentHistoryItemResponse {
    private String id;
    private String planNome;
    private String amountLabel;
    private String status;
    private String statusLabel;
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPlanNome() { return planNome; }
    public void setPlanNome(String planNome) { this.planNome = planNome; }
    public String getAmountLabel() { return amountLabel; }
    public void setAmountLabel(String amountLabel) { this.amountLabel = amountLabel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
