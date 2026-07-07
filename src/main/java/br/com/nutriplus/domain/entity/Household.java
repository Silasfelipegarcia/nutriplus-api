package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "households")
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_meal_plan_id")
    private MealPlan baseMealPlan;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Household() {
    }

    private Household(Builder builder) {
        this.id = builder.id;
        this.owner = builder.owner;
        this.baseMealPlan = builder.baseMealPlan;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public MealPlan getBaseMealPlan() {
        return baseMealPlan;
    }

    public void setBaseMealPlan(MealPlan baseMealPlan) {
        this.baseMealPlan = baseMealPlan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static class Builder {
        private Long id;
        private User owner;
        private MealPlan baseMealPlan;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder owner(User owner) {
            this.owner = owner;
            return this;
        }

        public Builder baseMealPlan(MealPlan baseMealPlan) {
            this.baseMealPlan = baseMealPlan;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Household build() {
            return new Household(this);
        }
    }
}
