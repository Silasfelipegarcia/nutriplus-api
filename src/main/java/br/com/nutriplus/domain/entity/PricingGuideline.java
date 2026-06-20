package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_guidelines")
public class PricingGuideline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_consultation_price_cents", nullable = false)
    private int minConsultationPriceCents;

    @Column(name = "max_consultation_price_cents", nullable = false)
    private int maxConsultationPriceCents;

    @Column(name = "suggested_price_cents", nullable = false)
    private int suggestedPriceCents;

    @Column(name = "platform_fee_percent", nullable = false)
    private java.math.BigDecimal platformFeePercent;

    @Column(name = "care_duration_days_default", nullable = false)
    private int careDurationDaysDefault;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PricingGuideline() {
    }

    public int getMinConsultationPriceCents() {
        return minConsultationPriceCents;
    }

    public int getMaxConsultationPriceCents() {
        return maxConsultationPriceCents;
    }

    public int getSuggestedPriceCents() {
        return suggestedPriceCents;
    }

    public java.math.BigDecimal getPlatformFeePercent() {
        return platformFeePercent;
    }

    public int getCareDurationDaysDefault() {
        return careDurationDaysDefault;
    }
}
