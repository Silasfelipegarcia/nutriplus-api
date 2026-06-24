package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.UserRole;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.PATIENT;

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

    @Column(name = "password_must_change", nullable = false)
    private boolean passwordMustChange = false;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    @Column(name = "terms_version", length = 20)
    private String termsVersion;

    @Column(name = "privacy_policy_accepted_at")
    private LocalDateTime privacyPolicyAcceptedAt;

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
        this.role = builder.role != null ? builder.role : UserRole.PATIENT;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
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
        private UserRole role;
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

        public Builder role(UserRole role) {
            this.role = role;
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
