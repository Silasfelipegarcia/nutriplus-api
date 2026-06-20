package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.ConsultationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_relationship_id", nullable = false)
    private CareRelationship careRelationship;

    @Column(name = "amount_cents", nullable = false)
    private int amountCents;

    @Column(name = "platform_fee_cents", nullable = false)
    private int platformFeeCents;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultationStatus status = ConsultationStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Consultation() {
    }

    public static Consultation create(CareRelationship care, int amountCents, int platformFeeCents) {
        Consultation c = new Consultation();
        c.careRelationship = care;
        c.amountCents = amountCents;
        c.platformFeeCents = platformFeeCents;
        return c;
    }

    public Long getId() {
        return id;
    }

    public CareRelationship getCareRelationship() {
        return careRelationship;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public int getPlatformFeeCents() {
        return platformFeeCents;
    }

    public int getNetCents() {
        return amountCents - platformFeeCents;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public ConsultationStatus getStatus() {
        return status;
    }

    public void setStatus(ConsultationStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
