package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.RegistrationSource;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.domain.enums.UserRole;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.PATIENT;

    @Column(name = "login_enabled", nullable = false)
    private boolean loginEnabled = false;

    @Column(name = "login_enabled_at")
    private LocalDateTime loginEnabledAt;

    @Column(name = "login_enabled_by")
    private Long loginEnabledBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_source", nullable = false, length = 32)
    private RegistrationSource registrationSource = RegistrationSource.OPEN;

    @Column(name = "acquisition_source", length = 64)
    private String acquisitionSource;

    @Column(name = "acquisition_medium", length = 64)
    private String acquisitionMedium;

    @Column(name = "acquisition_campaign", length = 128)
    private String acquisitionCampaign;

    @Column(name = "acquisition_landing", length = 128)
    private String acquisitionLanding;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "photo_url", columnDefinition = "LONGTEXT")
    private String photoUrl;

    @Column(name = "photo_thumbnail_url", columnDefinition = "MEDIUMTEXT")
    private String photoThumbnailUrl;

    @Column(name = "cpf_hash", length = 64, unique = true)
    private String cpfHash;

    @Column(name = "cpf_encrypted", length = 512)
    private String cpfEncrypted;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "password_reset_token_hash", length = 64)
    private String passwordResetTokenHash;

    @Column(name = "password_reset_expires_at")
    private Instant passwordResetExpiresAt;

    @Column(name = "password_must_change", nullable = false)
    private boolean passwordMustChange = false;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    @Column(name = "terms_version", length = 20)
    private String termsVersion;

    @Column(name = "privacy_policy_accepted_at")
    private LocalDateTime privacyPolicyAcceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false, length = 30)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    @Column(name = "plan_valid_until")
    private Instant planValidUntil;

    @Column(name = "plan_cancelled_at")
    private Instant planCancelledAt;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew = false;

    @Column(name = "default_card_id", length = 50)
    private String defaultCardId;

    @Column(name = "trial_utilizado", nullable = false)
    private boolean trialUtilizado = false;

    @Column(name = "trial_ate")
    private Instant trialAte;

    @Column(name = "mp_customer_id", length = 100)
    private String mpCustomerId;

    @Column(name = "athlete_grace_until")
    private Instant athleteGraceUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected User() {
    }

    private User(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.email = builder.email;
        this.contactPhone = builder.contactPhone;
        this.role = builder.role != null ? builder.role : UserRole.PATIENT;
        this.loginEnabled = builder.loginEnabled;
        this.loginEnabledAt = builder.loginEnabledAt;
        this.loginEnabledBy = builder.loginEnabledBy;
        this.registrationSource = builder.registrationSource != null
                ? builder.registrationSource
                : RegistrationSource.OPEN;
        this.acquisitionSource = builder.acquisitionSource;
        this.acquisitionMedium = builder.acquisitionMedium;
        this.acquisitionCampaign = builder.acquisitionCampaign;
        this.acquisitionLanding = builder.acquisitionLanding;
        this.passwordHash = builder.passwordHash;
        this.photoUrl = builder.photoUrl;
        this.photoThumbnailUrl = builder.photoThumbnailUrl;
        this.failedLoginAttempts = builder.failedLoginAttempts;
        this.passwordMustChange = builder.passwordMustChange;
        this.termsAcceptedAt = builder.termsAcceptedAt;
        this.termsVersion = builder.termsVersion;
        this.privacyPolicyAcceptedAt = builder.privacyPolicyAcceptedAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isLoginEnabled() {
        return loginEnabled;
    }

    public void setLoginEnabled(boolean loginEnabled) {
        this.loginEnabled = loginEnabled;
    }

    public LocalDateTime getLoginEnabledAt() {
        return loginEnabledAt;
    }

    public void setLoginEnabledAt(LocalDateTime loginEnabledAt) {
        this.loginEnabledAt = loginEnabledAt;
    }

    public Long getLoginEnabledBy() {
        return loginEnabledBy;
    }

    public void setLoginEnabledBy(Long loginEnabledBy) {
        this.loginEnabledBy = loginEnabledBy;
    }

    public RegistrationSource getRegistrationSource() {
        return registrationSource;
    }

    public void setRegistrationSource(RegistrationSource registrationSource) {
        this.registrationSource = registrationSource;
    }

    public String getAcquisitionSource() {
        return acquisitionSource;
    }

    public void setAcquisitionSource(String acquisitionSource) {
        this.acquisitionSource = acquisitionSource;
    }

    public String getAcquisitionMedium() {
        return acquisitionMedium;
    }

    public void setAcquisitionMedium(String acquisitionMedium) {
        this.acquisitionMedium = acquisitionMedium;
    }

    public String getAcquisitionCampaign() {
        return acquisitionCampaign;
    }

    public void setAcquisitionCampaign(String acquisitionCampaign) {
        this.acquisitionCampaign = acquisitionCampaign;
    }

    public String getAcquisitionLanding() {
        return acquisitionLanding;
    }

    public void setAcquisitionLanding(String acquisitionLanding) {
        this.acquisitionLanding = acquisitionLanding;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoThumbnailUrl() {
        return photoThumbnailUrl;
    }

    public void setPhotoThumbnailUrl(String photoThumbnailUrl) {
        this.photoThumbnailUrl = photoThumbnailUrl;
    }

    public String getCpfHash() {
        return cpfHash;
    }

    public void setCpfHash(String cpfHash) {
        this.cpfHash = cpfHash;
    }

    public String getCpfEncrypted() {
        return cpfEncrypted;
    }

    public void setCpfEncrypted(String cpfEncrypted) {
        this.cpfEncrypted = cpfEncrypted;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public String getPasswordResetTokenHash() {
        return passwordResetTokenHash;
    }

    public void setPasswordResetTokenHash(String passwordResetTokenHash) {
        this.passwordResetTokenHash = passwordResetTokenHash;
    }

    public Instant getPasswordResetExpiresAt() {
        return passwordResetExpiresAt;
    }

    public void setPasswordResetExpiresAt(Instant passwordResetExpiresAt) {
        this.passwordResetExpiresAt = passwordResetExpiresAt;
    }

    public boolean isPasswordResetTokenValid() {
        return passwordResetTokenHash != null
                && passwordResetExpiresAt != null
                && passwordResetExpiresAt.isAfter(Instant.now());
    }

    public boolean isPasswordMustChange() {
        return passwordMustChange;
    }

    public void setPasswordMustChange(boolean passwordMustChange) {
        this.passwordMustChange = passwordMustChange;
    }

    public LocalDateTime getTermsAcceptedAt() {
        return termsAcceptedAt;
    }

    public void setTermsAcceptedAt(LocalDateTime termsAcceptedAt) {
        this.termsAcceptedAt = termsAcceptedAt;
    }

    public String getTermsVersion() {
        return termsVersion;
    }

    public void setTermsVersion(String termsVersion) {
        this.termsVersion = termsVersion;
    }

    public LocalDateTime getPrivacyPolicyAcceptedAt() {
        return privacyPolicyAcceptedAt;
    }

    public void setPrivacyPolicyAcceptedAt(LocalDateTime privacyPolicyAcceptedAt) {
        this.privacyPolicyAcceptedAt = privacyPolicyAcceptedAt;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public Instant getPlanValidUntil() {
        return planValidUntil;
    }

    public void setPlanValidUntil(Instant planValidUntil) {
        this.planValidUntil = planValidUntil;
    }

    public Instant getPlanCancelledAt() {
        return planCancelledAt;
    }

    public void setPlanCancelledAt(Instant planCancelledAt) {
        this.planCancelledAt = planCancelledAt;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public String getDefaultCardId() {
        return defaultCardId;
    }

    public void setDefaultCardId(String defaultCardId) {
        this.defaultCardId = defaultCardId;
    }

    public boolean isTrialUtilizado() {
        return trialUtilizado;
    }

    public void setTrialUtilizado(boolean trialUtilizado) {
        this.trialUtilizado = trialUtilizado;
    }

    public Instant getTrialAte() {
        return trialAte;
    }

    public void setTrialAte(Instant trialAte) {
        this.trialAte = trialAte;
    }

    public String getMpCustomerId() {
        return mpCustomerId;
    }

    public void setMpCustomerId(String mpCustomerId) {
        this.mpCustomerId = mpCustomerId;
    }

    public Instant getAthleteGraceUntil() {
        return athleteGraceUntil;
    }

    public void setAthleteGraceUntil(Instant athleteGraceUntil) {
        this.athleteGraceUntil = athleteGraceUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private Long id;
        private String name;
        private String email;
        private String contactPhone;
        private UserRole role;
        private boolean loginEnabled;
        private LocalDateTime loginEnabledAt;
        private Long loginEnabledBy;
        private RegistrationSource registrationSource;
        private String acquisitionSource;
        private String acquisitionMedium;
        private String acquisitionCampaign;
        private String acquisitionLanding;
        private String passwordHash;
        private String photoUrl;
        private String photoThumbnailUrl;
        private String cpfHash;
        private String cpfEncrypted;
        private int failedLoginAttempts;
        private boolean passwordMustChange;
        private LocalDateTime termsAcceptedAt;
        private String termsVersion;
        private LocalDateTime privacyPolicyAcceptedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder contactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
            return this;
        }

        public Builder role(UserRole role) {
            this.role = role;
            return this;
        }

        public Builder loginEnabled(boolean loginEnabled) {
            this.loginEnabled = loginEnabled;
            return this;
        }

        public Builder loginEnabledAt(LocalDateTime loginEnabledAt) {
            this.loginEnabledAt = loginEnabledAt;
            return this;
        }

        public Builder loginEnabledBy(Long loginEnabledBy) {
            this.loginEnabledBy = loginEnabledBy;
            return this;
        }

        public Builder registrationSource(RegistrationSource registrationSource) {
            this.registrationSource = registrationSource;
            return this;
        }

        public Builder acquisitionSource(String acquisitionSource) {
            this.acquisitionSource = acquisitionSource;
            return this;
        }

        public Builder acquisitionMedium(String acquisitionMedium) {
            this.acquisitionMedium = acquisitionMedium;
            return this;
        }

        public Builder acquisitionCampaign(String acquisitionCampaign) {
            this.acquisitionCampaign = acquisitionCampaign;
            return this;
        }

        public Builder acquisitionLanding(String acquisitionLanding) {
            this.acquisitionLanding = acquisitionLanding;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder photoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
            return this;
        }

        public Builder photoThumbnailUrl(String photoThumbnailUrl) {
            this.photoThumbnailUrl = photoThumbnailUrl;
            return this;
        }

        public Builder cpfHash(String cpfHash) {
            this.cpfHash = cpfHash;
            return this;
        }

        public Builder cpfEncrypted(String cpfEncrypted) {
            this.cpfEncrypted = cpfEncrypted;
            return this;
        }

        public Builder failedLoginAttempts(int failedLoginAttempts) {
            this.failedLoginAttempts = failedLoginAttempts;
            return this;
        }

        public Builder passwordMustChange(boolean passwordMustChange) {
            this.passwordMustChange = passwordMustChange;
            return this;
        }

        public Builder termsAcceptedAt(LocalDateTime termsAcceptedAt) {
            this.termsAcceptedAt = termsAcceptedAt;
            return this;
        }

        public Builder termsVersion(String termsVersion) {
            this.termsVersion = termsVersion;
            return this;
        }

        public Builder privacyPolicyAcceptedAt(LocalDateTime privacyPolicyAcceptedAt) {
            this.privacyPolicyAcceptedAt = privacyPolicyAcceptedAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
