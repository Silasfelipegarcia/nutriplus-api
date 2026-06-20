package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "nutritionist_invites")
public class NutritionistInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutritionist_id", nullable = false)
    private Nutritionist nutritionist;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "use_count", nullable = false)
    private int useCount = 0;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected NutritionistInvite() {
    }

    public static NutritionistInvite create(Nutritionist nutritionist, String code) {
        NutritionistInvite invite = new NutritionistInvite();
        invite.nutritionist = nutritionist;
        invite.code = code;
        return invite;
    }

    public Long getId() {
        return id;
    }

    public Nutritionist getNutritionist() {
        return nutritionist;
    }

    public void setNutritionist(Nutritionist nutritionist) {
        this.nutritionist = nutritionist;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public int getUseCount() {
        return useCount;
    }

    public void incrementUseCount() {
        this.useCount++;
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

    public boolean isValid() {
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        return maxUses == null || useCount < maxUses;
    }
}
