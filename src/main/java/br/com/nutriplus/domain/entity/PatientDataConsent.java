package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_data_consents")
public class PatientDataConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutritionist_id", nullable = false)
    private Nutritionist nutritionist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_relationship_id", nullable = false)
    private CareRelationship careRelationship;

    @Column(nullable = false, length = 500)
    private String scopes = "PROFILE,MEASUREMENTS,MEAL_PLANS,CHECKINS,PROGRESS";

    @CreationTimestamp
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    protected PatientDataConsent() {
    }

    public static PatientDataConsent grant(User patient, Nutritionist nutritionist, CareRelationship care) {
        PatientDataConsent consent = new PatientDataConsent();
        consent.patient = patient;
        consent.nutritionist = nutritionist;
        consent.careRelationship = care;
        return consent;
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

    public CareRelationship getCareRelationship() {
        return careRelationship;
    }

    public String getScopes() {
        return scopes;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }
}
