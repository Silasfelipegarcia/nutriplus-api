package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.SubscriptionPlan;
import jakarta.persistence.*;

@Entity
@Table(name = "subscription_plan_catalog")
public class SubscriptionPlanCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_code", nullable = false, length = 30)
    private SubscriptionPlan planCode;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 512)
    private String description;

    @Column(name = "price_cents", nullable = false)
    private int priceCents;

    @Column(name = "period_days", nullable = false)
    private int periodDays;

    @Column(name = "price_suffix", length = 16)
    private String priceSuffix;

    @Column(name = "benefits_json", columnDefinition = "json")
    private String benefitsJson;

    @Column(name = "trial_available", nullable = false)
    private boolean trialAvailable;

    @Column(name = "contact_sales", nullable = false)
    private boolean contactSales;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "visible_in_catalog", nullable = false)
    private boolean visibleInCatalog;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private java.time.LocalDateTime updatedAt;

    protected SubscriptionPlanCatalog() {
    }

    public Long getId() { return id; }
    public SubscriptionPlan getPlanCode() { return planCode; }
    public void setPlanCode(SubscriptionPlan planCode) { this.planCode = planCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPriceCents() { return priceCents; }
    public void setPriceCents(int priceCents) { this.priceCents = priceCents; }
    public int getPeriodDays() { return periodDays; }
    public void setPeriodDays(int periodDays) { this.periodDays = periodDays; }
    public String getPriceSuffix() { return priceSuffix; }
    public void setPriceSuffix(String priceSuffix) { this.priceSuffix = priceSuffix; }
    public String getBenefitsJson() { return benefitsJson; }
    public void setBenefitsJson(String benefitsJson) { this.benefitsJson = benefitsJson; }
    public boolean isTrialAvailable() { return trialAvailable; }
    public void setTrialAvailable(boolean trialAvailable) { this.trialAvailable = trialAvailable; }
    public boolean isContactSales() { return contactSales; }
    public void setContactSales(boolean contactSales) { this.contactSales = contactSales; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isVisibleInCatalog() { return visibleInCatalog; }
    public void setVisibleInCatalog(boolean visibleInCatalog) { this.visibleInCatalog = visibleInCatalog; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
}
