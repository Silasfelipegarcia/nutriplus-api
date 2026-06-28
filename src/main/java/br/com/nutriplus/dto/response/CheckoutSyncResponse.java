package br.com.nutriplus.dto.response;

public class CheckoutSyncResponse {
    private String orderId;
    private String status;
    private String statusLabel;
    private String planNome;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
    public String getPlanNome() { return planNome; }
    public void setPlanNome(String planNome) { this.planNome = planNome; }
}
