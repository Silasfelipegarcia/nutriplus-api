package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "nutritionists")
public class Nutritionist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 20)
    private String crn;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 500)
    private String specialties;

    @Column(name = "service_modes", nullable = false, length = 64)
    private String serviceModes = "ONLINE,IN_PERSON";

    @Column(length = 120)
    private String city;

    @Column(name = "state_code", length = 2)
    private String stateCode;

    @Column(length = 120)
    private String neighborhood;

    @Column(name = "whatsapp_phone", length = 20)
    private String whatsappPhone;

    @Column(name = "consultation_price_cents", nullable = false)
    private int consultationPriceCents = 7900;

    @Column(name = "care_duration_days", nullable = false)
    private int careDurationDays = 30;

    @Column(name = "stripe_account_id")
    private String stripeAccountId;

    @Column(name = "stripe_onboarding_complete", nullable = false)
    private boolean stripeOnboardingComplete = false;

    @Column(name = "marketplace_visible", nullable = false)
    private boolean marketplaceVisible = true;

    @Column(name = "crn_verified", nullable = false)
    private boolean crnVerified = false;

    @Column(columnDefinition = "TEXT")
    private String formation;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(length = 500)
    private String approach;

    @Column(length = 128)
    private String languages;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Nutritionist() {
    }

    public static Nutritionist createFor(User user, String crn, String bio, String specialties,
                                         int priceCents, int careDays) {
        Nutritionist n = new Nutritionist();
        n.user = user;
        n.crn = crn;
        n.bio = bio;
        n.specialties = specialties;
        n.consultationPriceCents = priceCents;
        n.careDurationDays = careDays;
        n.crnVerified = false;
        return n;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getSpecialties() {
        return specialties;
    }

    public void setSpecialties(String specialties) {
        this.specialties = specialties;
    }

    public String getServiceModes() {
        return serviceModes;
    }

    public void setServiceModes(String serviceModes) {
        this.serviceModes = serviceModes;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getWhatsappPhone() {
        return whatsappPhone;
    }

    public void setWhatsappPhone(String whatsappPhone) {
        this.whatsappPhone = whatsappPhone;
    }

    public int getConsultationPriceCents() {
        return consultationPriceCents;
    }

    public void setConsultationPriceCents(int consultationPriceCents) {
        this.consultationPriceCents = consultationPriceCents;
    }

    public int getCareDurationDays() {
        return careDurationDays;
    }

    public void setCareDurationDays(int careDurationDays) {
        this.careDurationDays = careDurationDays;
    }

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }

    public boolean isStripeOnboardingComplete() {
        return stripeOnboardingComplete;
    }

    public void setStripeOnboardingComplete(boolean stripeOnboardingComplete) {
        this.stripeOnboardingComplete = stripeOnboardingComplete;
    }

    public boolean isMarketplaceVisible() {
        return marketplaceVisible;
    }

    public void setMarketplaceVisible(boolean marketplaceVisible) {
        this.marketplaceVisible = marketplaceVisible;
    }

    public boolean isCrnVerified() {
        return crnVerified;
    }

    public void setCrnVerified(boolean crnVerified) {
        this.crnVerified = crnVerified;
    }

    public String getFormation() {
        return formation;
    }

    public void setFormation(String formation) {
        this.formation = formation;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getApproach() {
        return approach;
    }

    public void setApproach(String approach) {
        this.approach = approach;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
