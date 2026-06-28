package br.com.nutriplus.dto.response;

public class CheckoutResponse {
    private String orderId;
    private String preferenceId;
    private String initPoint;
    private String sandboxInitPoint;
    private int amountCents;
    private String amountLabel;
    private boolean upgrade;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getPreferenceId() { return preferenceId; }
    public void setPreferenceId(String preferenceId) { this.preferenceId = preferenceId; }
    public String getInitPoint() { return initPoint; }
    public void setInitPoint(String initPoint) { this.initPoint = initPoint; }
    public String getSandboxInitPoint() { return sandboxInitPoint; }
    public void setSandboxInitPoint(String sandboxInitPoint) { this.sandboxInitPoint = sandboxInitPoint; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
    public String getAmountLabel() { return amountLabel; }
    public void setAmountLabel(String amountLabel) { this.amountLabel = amountLabel; }
    public boolean isUpgrade() { return upgrade; }
    public void setUpgrade(boolean upgrade) { this.upgrade = upgrade; }
}
