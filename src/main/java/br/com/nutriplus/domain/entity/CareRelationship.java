package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.CareRelationshipSource;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import br.com.nutriplus.domain.enums.PreferredCareMode;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "care_relationships")
public class CareRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutritionist_id", nullable = false)
    private Nutritionist nutritionist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareRelationshipStatus status = CareRelationshipStatus.PRE_ENGAGED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareRelationshipSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_care_mode")
    private PreferredCareMode preferredCareMode;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CareRelationship() {
    }

    public static CareRelationship create(User patient, Nutritionist nutritionist, CareRelationshipSource source) {
        CareRelationship cr = new CareRelationship();
        cr.patient = patient;
        cr.nutritionist = nutritionist;
        cr.source = source;
        cr.status = source == CareRelationshipSource.INVITE
                ? CareRelationshipStatus.PRE_ENGAGED
                : CareRelationshipStatus.PENDING_PAYMENT;
        return cr;
    }

    public Long getId() {
        return id;
    }

    public User getPatient() {
        return patient;
    }

    public Nutritionist getNutritionist() {
        return nutritionist;
    }

    public CareRelationshipStatus getStatus() {
        return status;
    }

    public void setStatus(CareRelationshipStatus status) {
        this.status = status;
    }

    public CareRelationshipSource getSource() {
        return source;
    }

    public PreferredCareMode getPreferredCareMode() {
        return preferredCareMode;
    }

    public void setPreferredCareMode(PreferredCareMode preferredCareMode) {
        this.preferredCareMode = preferredCareMode;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean allowsNutritionistAccess() {
        return status == CareRelationshipStatus.PRE_ENGAGED
                || status == CareRelationshipStatus.ACTIVE;
    }

    public boolean allowsChat() {
        return status == CareRelationshipStatus.ACTIVE;
    }
}
