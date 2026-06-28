package br.com.nutriplus.dto.response;

public class SavedCardResponse {
    private String id;
    private String brand;
    private String lastFourDigits;
    private String expirationMonth;
    private String expirationYear;
    private String holderName;
    private boolean defaultCard;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getLastFourDigits() { return lastFourDigits; }
    public void setLastFourDigits(String lastFourDigits) { this.lastFourDigits = lastFourDigits; }
    public String getExpirationMonth() { return expirationMonth; }
    public void setExpirationMonth(String expirationMonth) { this.expirationMonth = expirationMonth; }
    public String getExpirationYear() { return expirationYear; }
    public void setExpirationYear(String expirationYear) { this.expirationYear = expirationYear; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public boolean isDefaultCard() { return defaultCard; }
    public void setDefaultCard(boolean defaultCard) { this.defaultCard = defaultCard; }
}
